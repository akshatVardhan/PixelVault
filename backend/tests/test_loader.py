from pathlib import Path

import pytest

from backend.ml.base import MLModel
from backend.ml.loader import ModelRegistry, registry


class DummyModel(MLModel):
    def __init__(self, name: str = "dummy") -> None:
        super().__init__(name)

    def load(self) -> None:
        self._loaded = True

    def unload(self) -> None:
        self._loaded = False

    def predict(self, image_path: Path) -> dict:
        return {"model": self.name}


def test_register_and_get():
    r = ModelRegistry()
    r.register("dummy", lambda: DummyModel("dummy"))
    model = r.get("dummy")
    assert isinstance(model, DummyModel)
    assert model.loaded


def test_get_same_instance():
    r = ModelRegistry()
    r.register("dummy", lambda: DummyModel("dummy"))
    a = r.get("dummy")
    b = r.get("dummy")
    assert a is b


def test_get_unknown_raises():
    r = ModelRegistry()
    with pytest.raises(KeyError, match="unknown"):
        r.get("unknown")


def test_unload_removes_instance():
    r = ModelRegistry()
    r.register("dummy", lambda: DummyModel("dummy"))
    model = r.get("dummy")
    assert model.loaded
    r.unload("dummy")
    assert not model.loaded


def test_predict_via_registry(tmp_path):
    r = ModelRegistry()
    r.register("dummy", lambda: DummyModel("dummy"))
    img = tmp_path / "test.jpg"
    img.write_bytes(b"fake")
    result = r.predict("dummy", img)
    assert result == {"model": "dummy"}


def test_unload_all():
    r = ModelRegistry()
    r.register("a", lambda: DummyModel("a"))
    r.register("b", lambda: DummyModel("b"))
    r.get("a")
    r.get("b")
    r.unload_all()
    assert "a" not in r._instances
    assert "b" not in r._instances


def test_global_registry_is_singleton():
    assert isinstance(registry, ModelRegistry)
