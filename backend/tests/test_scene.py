import sys
from pathlib import Path
from unittest.mock import MagicMock, patch

import pytest

from backend.ml.scene.yolo import YOLOSceneModel


@pytest.fixture
def model():
    return YOLOSceneModel(device="cpu")


def test_load_unload(model):
    mock_ultralytics = MagicMock()
    with patch.dict(sys.modules, {"ultralytics": mock_ultralytics}):
        model.load()
        mock_ultralytics.YOLO.assert_called_once_with("yolov8n.pt")
        assert model.loaded
        model.unload()
        assert not model.loaded


def test_predict(tmp_path):
    img = tmp_path / "test.jpg"
    img.write_bytes(b"fake")

    mock_result = MagicMock()
    mock_result.boxes = []
    mock_result.names = {}

    model = YOLOSceneModel(device="cpu")
    model._loaded = True
    model._model = MagicMock()
    model._model.return_value = [mock_result]

    result = model.predict(img)
    assert result["model"] == "yolov8n"
    assert result["scene_labels"] == []
    assert result["detections"] == []


def test_predict_with_detections(tmp_path):
    img = tmp_path / "test.jpg"
    img.write_bytes(b"fake")

    mock_box = MagicMock()
    mock_box.cls.item.return_value = 0
    mock_box.conf.item.return_value = 0.95
    mock_box.xyxy[0].tolist.return_value = [10, 20, 100, 200]

    mock_result = MagicMock()
    mock_result.boxes = [mock_box]
    mock_result.names = {0: "person"}

    model = YOLOSceneModel(device="cpu")
    model._loaded = True
    model._model = MagicMock()
    model._model.return_value = [mock_result]

    result = model.predict(img)
    assert result["scene_labels"] == ["person"]
    assert result["detections"][0]["label"] == "person"
    assert result["detections"][0]["confidence"] == 0.95
