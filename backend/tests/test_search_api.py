from unittest.mock import patch

import numpy as np
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


def test_search_requires_query(client):
    response = client.get(
        "/api/v1/search",
        headers={"Authorization": "Bearer test-token"},
    )
    assert response.status_code == 422


def test_search_requires_auth(client):
    response = client.get("/api/v1/search?q=cat")
    assert response.status_code == 401


def test_search_empty_results(client):
    with patch("backend.ml.loader.registry.get") as mock_get:
        mock_model = type("Mock", (), {"encode_text": lambda self, t: np.array([[0.1, 0.2]])})()
        mock_get.return_value = mock_model
        response = client.get(
            "/api/v1/search?q=cat",
            headers={"Authorization": "Bearer test-token"},
        )
        assert response.status_code == 200
        assert response.json()["results"] == []


def test_search_tags_requires_tags(client):
    response = client.get(
        "/api/v1/search/tags",
        headers={"Authorization": "Bearer test-token"},
    )
    assert response.status_code == 422


def test_search_tags_empty(client):
    response = client.get(
        "/api/v1/search/tags?tags=",
        headers={"Authorization": "Bearer test-token"},
    )
    assert response.status_code == 200
    assert response.json()["results"] == []


def test_search_tags_requires_auth(client):
    response = client.get("/api/v1/search/tags?tags=person")
    assert response.status_code == 401
