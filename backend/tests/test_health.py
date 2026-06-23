from fastapi.testclient import TestClient

from backend.config import settings
from backend.main import app


def test_health_endpoint(tmp_path):
    settings.db_path = tmp_path / "test.db"
    settings.storage_path = tmp_path / "test_photos"
    client = TestClient(app)
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}
