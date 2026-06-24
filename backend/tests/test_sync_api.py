import io
from datetime import datetime, timezone

import pytest
from fastapi.testclient import TestClient

from backend.config import settings
from backend.main import app


@pytest.fixture
def client(tmp_path):
    settings.db_path = tmp_path / "test.db"
    settings.storage_path = tmp_path / "photos"
    settings.auth_token = "test-token"
    with TestClient(app) as c:
        yield c


def test_upload_photo(client):
    now = datetime.now(timezone.utc).isoformat()
    file = io.BytesIO(b"fake-image-content")
    response = client.post(
        "/api/v1/sync/upload",
        headers={"Authorization": "Bearer test-token"},
        files={"file": ("photo.jpg", file, "image/jpeg")},
        data={"filename": "photo.jpg", "hash": "abc123", "size": 18, "created_at": now},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "uploaded"
    assert "photo_id" in data


def test_upload_duplicate_photo(client):
    now = datetime.now(timezone.utc).isoformat()
    file = io.BytesIO(b"fake-image-content")
    client.post(
        "/api/v1/sync/upload",
        headers={"Authorization": "Bearer test-token"},
        files={"file": ("photo.jpg", file, "image/jpeg")},
        data={"filename": "photo.jpg", "hash": "dup123", "size": 18, "created_at": now},
    )
    file2 = io.BytesIO(b"fake-image-content")
    response = client.post(
        "/api/v1/sync/upload",
        headers={"Authorization": "Bearer test-token"},
        files={"file": ("photo.jpg", file2, "image/jpeg")},
        data={"filename": "photo.jpg", "hash": "dup123", "size": 18, "created_at": now},
    )
    assert response.status_code == 200
    assert response.json()["status"] == "duplicate"


def test_upload_invalid_date(client):
    file = io.BytesIO(b"fake")
    response = client.post(
        "/api/v1/sync/upload",
        headers={"Authorization": "Bearer test-token"},
        files={"file": ("photo.jpg", file, "image/jpeg")},
        data={"filename": "photo.jpg", "hash": "abc", "size": 4, "created_at": "bad-date"},
    )
    assert response.status_code == 422


def test_sync_status(client):
    response = client.get(
        "/api/v1/sync/status",
        headers={"Authorization": "Bearer test-token"},
    )
    assert response.status_code == 200
    data = response.json()
    assert "last_sync" in data
    assert "total_photos" in data
    assert "pending_ml" in data


def test_upload_requires_auth(client):
    response = client.post("/api/v1/sync/upload")
    assert response.status_code == 401


def test_sync_status_requires_auth(client):
    response = client.get("/api/v1/sync/status")
    assert response.status_code == 401
