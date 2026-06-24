import sys
from pathlib import Path
from unittest.mock import MagicMock, patch

import numpy as np
import pytest
import torch

from backend.ml.clip.encoder import CLIPEncoder


@pytest.fixture
def model():
    return CLIPEncoder(device="cpu")


def test_load_unload(model):
    mock_transformers = MagicMock()
    mock_model = MagicMock()
    mock_processor = MagicMock()
    mock_transformers.CLIPModel.from_pretrained.return_value = mock_model
    mock_transformers.CLIPProcessor.from_pretrained.return_value = mock_processor
    with patch.dict(sys.modules, {"transformers": mock_transformers}):
        model.load()
        mock_transformers.CLIPModel.from_pretrained.assert_called_once_with("openai/clip-vit-base-patch32")
        assert model.loaded
        model.unload()
        assert not model.loaded


def test_predict(tmp_path):
    img = tmp_path / "test.jpg"
    img.write_bytes(b"fake")

    with patch("PIL.Image.open") as mock_open:
        mock_open.return_value.convert.return_value = MagicMock()
        real_embedding = torch.randn(1, 512)
        mock_processor = MagicMock()
        mock_model = MagicMock()
        mock_model.get_image_features.return_value = real_embedding
        model = CLIPEncoder(device="cpu")
        model._model = mock_model
        model._processor = mock_processor
        model._loaded = True
        result = model.predict(img)
        assert result["model"] == "clip-vit-base-patch32"
        assert isinstance(result["embedding"], bytes)
        assert len(result["embedding"]) == 512 * 4


def test_encode_text():
    real_embedding = torch.randn(1, 512)
    mock_model = MagicMock()
    mock_model.get_text_features.return_value = real_embedding
    model = CLIPEncoder(device="cpu")
    model._model = mock_model
    model._processor = MagicMock()
    model._loaded = True
    result = model.encode_text("a cat on a beach")
    assert isinstance(result, np.ndarray)
    assert result.shape == (1, 512)
