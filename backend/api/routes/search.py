import numpy as np
from fastapi import APIRouter, Depends, Query
from numpy.linalg import norm

from backend.api.auth import get_current_user
from backend.db.connection import get_db
from backend.ml.loader import registry

router = APIRouter(dependencies=[Depends(get_current_user)])


@router.get("/search")
async def search_photos(q: str = Query(..., min_length=1)):
    model = registry.get("clip")
    text_emb = model.encode_text(q)

    async with get_db() as db:
        cursor = await db.execute(
            "SELECT id, filename, path, clip_embedding FROM photos WHERE clip_embedding IS NOT NULL"
        )
        rows = await cursor.fetchall()

    if not rows:
        return {"query": q, "results": []}

    results = []
    for row in rows:
        photo_id, filename, path, blob = row
        img_emb = np.frombuffer(blob, dtype=np.float32).reshape(1, -1)
        sim = float(np.dot(text_emb, img_emb.T) / (norm(text_emb) * norm(img_emb) + 1e-8))
        results.append((sim, {"id": photo_id, "filename": filename, "path": path}))

    results.sort(key=lambda x: x[0], reverse=True)
    return {"query": q, "results": [r[1] for r in results[:50]]}


@router.get("/search/tags")
async def search_by_tags(
    tags: str = Query(..., description="Comma-separated tags"),
    type: str | None = Query(None, description="Tag type filter (scene, food, etc.)"),
):
    tag_list = [t.strip() for t in tags.split(",") if t.strip()]
    if not tag_list:
        return {"tags": [], "results": []}

    placeholders = ",".join("?" for _ in tag_list)
    query = f"""
        SELECT DISTINCT p.id, p.filename, p.path, p.created_at
        FROM photos p
        JOIN tags t ON t.photo_id = p.id
        WHERE t.label IN ({placeholders})
    """
    params: list = [*tag_list]
    if type:
        query += " AND t.type = ?"
        params.append(type)
    query += " ORDER BY p.created_at DESC"

    async with get_db() as db:
        cursor = await db.execute(query, params)
        rows = await cursor.fetchall()

    return {
        "tags": tag_list,
        "results": [
            {"id": r[0], "filename": r[1], "path": r[2], "created_at": r[3]}
            for r in rows
        ],
    }
