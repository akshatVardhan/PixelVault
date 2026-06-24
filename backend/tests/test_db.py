import aiosqlite
import pytest

from backend.config import settings
from backend.db.connection import get_db, init_db


PHOTOS_COLS = {"id", "filename", "hash", "size", "created_at", "synced_at", "path", "clip_embedding"}
TAGS_COLS = {"id", "photo_id", "type", "label", "confidence"}
FACES_COLS = {"id", "photo_id", "cluster_id", "embedding_path", "bbox"}
CLUSTERS_COLS = {"id", "name", "created_at"}


@pytest.mark.asyncio
async def test_init_db_creates_tables(tmp_path):
    db_path = tmp_path / "test.db"
    settings.db_path = db_path
    settings.storage_path = tmp_path / "photos"
    await init_db()
    async with aiosqlite.connect(db_path) as db:
        cursor = await db.execute(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name"
        )
        rows = await cursor.fetchall()
    names = {r[0] for r in rows}
    assert names == {"clusters", "faces", "photos", "tags"}


@pytest.mark.asyncio
async def test_get_db_yields_working_connection(tmp_path):
    db_path = tmp_path / "test.db"
    settings.db_path = db_path
    settings.storage_path = tmp_path / "photos"
    await init_db()
    async with get_db() as db:
        cursor = await db.execute("SELECT 1")
        (val,) = await cursor.fetchone()
    assert val == 1


@pytest.mark.asyncio
async def test_pragma_foreign_keys(tmp_path):
    db_path = tmp_path / "test.db"
    settings.db_path = db_path
    settings.storage_path = tmp_path / "photos"
    await init_db()
    async with get_db() as db:
        cursor = await db.execute("PRAGMA foreign_keys")
        (fk,) = await cursor.fetchone()
    assert fk == 1


@pytest.mark.asyncio
async def test_pragma_wal_mode(tmp_path):
    db_path = tmp_path / "test.db"
    settings.db_path = db_path
    settings.storage_path = tmp_path / "photos"
    await init_db()
    async with get_db() as db:
        cursor = await db.execute("PRAGMA journal_mode")
        (jm,) = await cursor.fetchone()
    assert jm == "wal"


@pytest.mark.asyncio
async def test_photos_table_has_correct_columns(tmp_path):
    db_path = tmp_path / "test.db"
    settings.db_path = db_path
    settings.storage_path = tmp_path / "photos"
    await init_db()
    async with aiosqlite.connect(db_path) as db:
        cursor = await db.execute("PRAGMA table_info(photos)")
        rows = await cursor.fetchall()
    cols = {r[1] for r in rows}
    assert cols == PHOTOS_COLS


@pytest.mark.asyncio
async def test_tags_table_has_correct_columns(tmp_path):
    db_path = tmp_path / "test.db"
    settings.db_path = db_path
    settings.storage_path = tmp_path / "photos"
    await init_db()
    async with aiosqlite.connect(db_path) as db:
        cursor = await db.execute("PRAGMA table_info(tags)")
        rows = await cursor.fetchall()
    cols = {r[1] for r in rows}
    assert cols == TAGS_COLS


@pytest.mark.asyncio
async def test_faces_table_has_correct_columns(tmp_path):
    db_path = tmp_path / "test.db"
    settings.db_path = db_path
    settings.storage_path = tmp_path / "photos"
    await init_db()
    async with aiosqlite.connect(db_path) as db:
        cursor = await db.execute("PRAGMA table_info(faces)")
        rows = await cursor.fetchall()
    cols = {r[1] for r in rows}
    assert cols == FACES_COLS


@pytest.mark.asyncio
async def test_clusters_table_has_correct_columns(tmp_path):
    db_path = tmp_path / "test.db"
    settings.db_path = db_path
    settings.storage_path = tmp_path / "photos"
    await init_db()
    async with aiosqlite.connect(db_path) as db:
        cursor = await db.execute("PRAGMA table_info(clusters)")
        rows = await cursor.fetchall()
    cols = {r[1] for r in rows}
    assert cols == CLUSTERS_COLS
