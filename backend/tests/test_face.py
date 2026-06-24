import sys
from pathlib import Path
from unittest.mock import MagicMock, patch

import numpy as np
import pytest

from backend.ml.face.cluster import cosine_similarity, load_embedding
from backend.ml.face.detector import FaceDetector


@pytest.fixture
def model():
    return FaceDetector(device="cpu")


def test_load_unload(model):
    mock_insightface = MagicMock()
    mock_app = MagicMock()
    mock_insightface.app.FaceAnalysis.return_value = mock_app
    with patch.dict(sys.modules, {"insightface": mock_insightface, "insightface.app": mock_insightface.app}):
        model.load()
        mock_insightface.app.FaceAnalysis.assert_called_once()
        mock_app.prepare.assert_called_once_with(ctx_id=-1)
        assert model.loaded
        model.unload()
        assert not model.loaded


def test_predict_no_faces(tmp_path):
    img = tmp_path / "test.jpg"
    img.write_bytes(b"fake")

    mock_cv2 = MagicMock()
    mock_cv2.imread.return_value = np.zeros((100, 100, 3), dtype=np.uint8)
    with patch.dict(sys.modules, {"cv2": mock_cv2}):
        from backend.ml.face.detector import FaceDetector as FD
        model = FD(device="cpu")
        mock_app = MagicMock()
        mock_app.get.return_value = []
        model._app = mock_app
        model._loaded = True
        result = model.predict(img)
        assert result["faces"] == []


def test_predict_with_faces(tmp_path):
    img = tmp_path / "test.jpg"
    img.write_bytes(b"fake")

    mock_face = MagicMock()
    mock_face.embedding = np.random.randn(512).astype(np.float32)
    mock_face.bbox = np.array([10, 20, 100, 200], dtype=np.float64)
    mock_face.det_score = 0.99
    mock_face.face_id = 0

    mock_cv2 = MagicMock()
    mock_cv2.imread.return_value = np.zeros((100, 100, 3), dtype=np.uint8)
    with (
        patch.dict(sys.modules, {"cv2": mock_cv2}),
        patch("backend.ml.face.detector.np.save") as mock_npsave,
    ):
        from backend.ml.face.detector import FaceDetector as FD
        model = FD(device="cpu")
        mock_app = MagicMock()
        mock_app.get.return_value = [mock_face]
        model._app = mock_app
        model._loaded = True
        result = model.predict(img)
        assert len(result["faces"]) == 1
        assert result["faces"][0]["confidence"] == 0.99
        assert result["faces"][0]["bbox"] == [10, 20, 100, 200]
        mock_npsave.assert_called_once()


def test_cosine_similarity():
    a = np.array([1.0, 0.0, 0.0])
    b = np.array([1.0, 0.0, 0.0])
    assert cosine_similarity(a, b) == pytest.approx(1.0)
    c = np.array([-1.0, 0.0, 0.0])
    assert cosine_similarity(a, c) == pytest.approx(-1.0)
    d = np.array([0.0, 1.0, 0.0])
    assert cosine_similarity(a, d) == pytest.approx(0.0)


def test_cosine_similarity_zero_vector():
    a = np.array([1.0, 0.0])
    b = np.array([0.0, 0.0])
    sim = cosine_similarity(a, b)
    assert sim == 0.0


def test_load_embedding(tmp_path):
    emb_path = tmp_path / "emb.npy"
    expected = np.array([0.5, 0.3, 0.1], dtype=np.float32)
    np.save(str(emb_path), expected)
    loaded = load_embedding(str(emb_path))
    np.testing.assert_array_equal(loaded, expected)
