import pytest
from fastapi.testclient import TestClient

from backend.config import settings
from backend.db.connection import init_db
from backend.main import app


@pytest.fixture
def client(tmp_path):
    settings.db_path = tmp_path / "test.db"
    settings.storage_path = tmp_path / "photos"
    settings.auth_token = "test-token"
    with TestClient(app) as c:
        yield c


def test_list_clusters_empty(client):
    response = client.get(
        "/api/v1/faces/clusters",
        headers={"Authorization": "Bearer test-token"},
    )
    assert response.status_code == 200
    assert response.json()["clusters"] == []


def test_cluster_not_found(client):
    response = client.get(
        "/api/v1/faces/clusters/999/photos",
        headers={"Authorization": "Bearer test-token"},
    )
    assert response.status_code == 404


def test_rename_nonexistent_cluster(client):
    response = client.put(
        "/api/v1/faces/clusters/999/name",
        headers={"Authorization": "Bearer test-token", "Content-Type": "application/json"},
        json={"name": "test"},
    )
    assert response.status_code == 404


def test_cluster_endpoint(client):
    response = client.post(
        "/api/v1/faces/cluster",
        headers={"Authorization": "Bearer test-token"},
    )
    assert response.status_code == 200
    data = response.json()
    assert "clusters" in data
    assert "faces" in data


def test_faces_api_requires_auth(client):
    response = client.get("/api/v1/faces/clusters")
    assert response.status_code == 401
