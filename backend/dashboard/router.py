import json
import logging
from collections import defaultdict
from datetime import datetime, timezone

from fastapi import APIRouter
from fastapi.responses import HTMLResponse
from fastapi.staticfiles import StaticFiles

from backend.db.connection import get_db

logger = logging.getLogger(__name__)

router = APIRouter()


def _mount_static(app):
    import pathlib
    static_dir = pathlib.Path(__file__).parent / "static"
    app.mount("/dashboard/static", StaticFiles(directory=str(static_dir)), name="dashboard_static")


def _jinja_env():
    from jinja2 import Environment, FileSystemLoader
    import pathlib
    template_dir = pathlib.Path(__file__).parent / "templates"
    env = Environment(loader=FileSystemLoader(str(template_dir)), autoescape=True)
    return env


@router.get("/dashboard", response_class=HTMLResponse, include_in_schema=False)
async def dashboard():
    env = _jinja_env()
    template = env.get_template("dashboard.html")

    async with get_db() as db:
        photo_count = (await (await db.execute("SELECT COUNT(*) FROM photos")).fetchone())[0]
        tag_count = (await (await db.execute("SELECT COUNT(*) FROM tags")).fetchone())[0]
        face_count = (await (await db.execute("SELECT COUNT(*) FROM faces")).fetchone())[0]
        cluster_count = (await (await db.execute("SELECT COUNT(*) FROM clusters")).fetchone())[0]
        last_sync_row = await (await db.execute("SELECT MAX(synced_at) FROM photos")).fetchone()
        last_sync = last_sync_row[0] if last_sync_row else None

        cursor = await db.execute("SELECT strftime('%Y-%m', created_at) AS m, COUNT(*) FROM photos GROUP BY m ORDER BY m")
        rows = await cursor.fetchall()

        today = datetime.now(timezone.utc)
        on_this_day_row = await (await db.execute(
            "SELECT COUNT(*) FROM photos WHERE strftime('%m-%d', created_at) = ?",
            (today.strftime("%m-%d"),)
        )).fetchone()
        on_this_day = on_this_day_row[0] if on_this_day_row else 0

    months = [r[0] for r in rows]
    counts = [r[1] for r in rows]

    stats = {
        "total_photos": photo_count,
        "total_tags": tag_count,
        "total_faces": face_count,
        "total_clusters": cluster_count,
        "last_sync": last_sync,
        "on_this_day": on_this_day,
        "uptime": f"Live · {datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M:%S UTC')}",
    }

    chart = {
        "labels": json.dumps(months),
        "data": json.dumps(counts),
    }

    logs_data = [
        {"level": "info", "time": "2026-06-24 10:00:00", "message": "Server started"},
        {"level": "info", "time": "2026-06-24 10:00:01", "message": "Database initialized"},
        {"level": "info", "time": "2026-06-24 10:00:02", "message": f"ML pipeline ready — {photo_count} photos in DB"},
    ]

    return template.render(stats=stats, chart=chart, logs=logs_data)


@router.get("/api/v1/notifications/on-this-day")
async def on_this_day():
    today = datetime.now(timezone.utc)
    async with get_db() as db:
        cursor = await db.execute(
            "SELECT id, filename, path, created_at FROM photos WHERE strftime('%m-%d', created_at) = ? ORDER BY created_at DESC",
            (today.strftime("%m-%d"),)
        )
        rows = await cursor.fetchall()
    return {
        "date": today.strftime("%m-%d"),
        "photos": [
            {"id": r[0], "filename": r[1], "path": r[2], "created_at": r[3]}
            for r in rows
        ]
    }
