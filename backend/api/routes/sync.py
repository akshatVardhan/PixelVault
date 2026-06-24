from datetime import datetime
from pathlib import Path

from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile

from backend.api.auth import get_current_user
from backend.config import settings
from backend.db.connection import get_db

router = APIRouter(dependencies=[Depends(get_current_user)])


@router.post("/sync/upload")
async def upload_photo(
    file: UploadFile = File(...),
    filename: str = Form(...),
    hash: str = Form(...),
    size: int = Form(...),
    created_at: str = Form(...),
) -> dict:
    try:
        dt = datetime.fromisoformat(created_at)
    except ValueError:
        raise HTTPException(status_code=422, detail="Invalid created_at format")

    async with get_db() as db:
        cursor = await db.execute("SELECT id FROM photos WHERE hash = ?", (hash,))
        row = await cursor.fetchone()
        if row is not None:
            return {"status": "duplicate", "photo_id": row[0]}

        year = dt.strftime("%Y")
        month = dt.strftime("%m")
        dest: Path = settings.storage_path / year / month
        dest.mkdir(parents=True, exist_ok=True)

        file_path = dest / filename
        content = await file.read()
        file_path.write_bytes(content)

        cursor = await db.execute(
            "INSERT INTO photos (filename, hash, size, created_at, synced_at, path) VALUES (?, ?, ?, ?, datetime('now'), ?)",
            (filename, hash, size, created_at, str(file_path)),
        )
        await db.commit()
        return {"status": "uploaded", "photo_id": cursor.lastrowid}


@router.get("/sync/status")
async def sync_status() -> dict:
    async with get_db() as db:
        cursor = await db.execute("SELECT COUNT(*), MAX(synced_at) FROM photos")
        row = await cursor.fetchone()
        return {
            "last_sync": row[1] or None,
            "total_photos": row[0],
            "pending_ml": 0,
        }
