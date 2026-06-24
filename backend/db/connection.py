from collections.abc import AsyncGenerator
from contextlib import asynccontextmanager

import aiosqlite

from backend.config import settings
from backend.db.schema import SQL_CREATE_TABLES


@asynccontextmanager
async def get_db() -> AsyncGenerator[aiosqlite.Connection, None]:
    """Open an async SQLite connection with WAL mode and foreign keys enabled."""
    async with aiosqlite.connect(settings.db_path) as db:
        await db.execute("PRAGMA journal_mode=WAL")
        await db.execute("PRAGMA foreign_keys=ON")
        yield db


MIGRATIONS = [
    "ALTER TABLE photos ADD COLUMN clip_embedding BLOB",
]


async def init_db() -> None:
    """Initialize the database: create data dir, run DDL, and commit."""
    settings.db_path.parent.mkdir(parents=True, exist_ok=True)
    settings.storage_path.mkdir(parents=True, exist_ok=True)
    async with aiosqlite.connect(settings.db_path) as db:
        await db.executescript(SQL_CREATE_TABLES)
        for migration in MIGRATIONS:
            try:
                await db.execute(migration)
            except Exception:
                pass
        await db.commit()
