from pathlib import Path

import pytest

from backend.ml.base import MLModel


class ConcreteModel(MLModel):
    def load(self) -> None:
        self._loaded = True

    def unload(self) -> None:
        self._loaded = False

    def predict(self, image_path: Path) -> dict:
        return {"model": self.name, "path": str(image_path)}


def test_mlmodel_abstract_cannot_instantiate():
    with pytest.raises(TypeError):
        MLModel("test")  # type: ignore


def test_mlmodel_concrete_defaults():
    model = ConcreteModel("test")
    assert model.name == "test"
    assert not model.loaded
    model.load()
    assert model.loaded
    model.unload()
    assert not model.loaded


def test_mlmodel_predict(tmp_path):
    img = tmp_path / "test.jpg"
    img.write_bytes(b"fake")
    model = ConcreteModel("test")
    model.load()
    result = model.predict(img)
    assert result == {"model": "test", "path": str(img)}
