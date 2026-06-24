from pathlib import Path
from unittest.mock import MagicMock, patch

import pytest
import torch
from PIL import Image

from backend.ml.food.classifier import FoodClassifier, FOOD_LABELS


@pytest.fixture
def model():
    return FoodClassifier(device="cpu")


def test_load_unload(model):
    with patch("torchvision.models.efficientnet_v2_s") as mock_efn:
        mock_model = MagicMock()
        mock_efn.return_value = mock_model
        model.load()
        mock_efn.assert_called_once()
        assert model.loaded
        model.unload()
        assert not model.loaded


def _real_image(tmp_path: Path) -> Path:
    path = tmp_path / "test.jpg"
    Image.new("RGB", (224, 224), color="red").save(str(path))
    return path


def test_predict(tmp_path):
    img = _real_image(tmp_path)
    model = FoodClassifier(device="cpu")
    logits = torch.zeros(len(FOOD_LABELS))
    logits[2] = 1.0
    mock_model = MagicMock()
    mock_model.return_value = logits.unsqueeze(0)
    model._model = mock_model
    model._loaded = True
    result = model.predict(img)
    assert result["model"] == "efficientnet_v2_s"
    assert result["food_label"] in FOOD_LABELS
    assert 0 <= result["confidence"] <= 1


def test_predict_unknown_class(tmp_path):
    img = _real_image(tmp_path)
    FOOD_LEN = len(FOOD_LABELS)
    logits = torch.zeros(FOOD_LEN + 10)
    logits[FOOD_LEN + 3] = 1.0
    model = FoodClassifier(device="cpu")
    model._model = MagicMock()
    model._model.return_value = logits.unsqueeze(0)
    model._loaded = True
    result = model.predict(img)
    assert result["food_label"] == f"class_{FOOD_LEN + 3}"
