from collections.abc import Callable
from pathlib import Path

from backend.ml.base import MLModel


class ModelRegistry:
    def __init__(self) -> None:
        self._factories: dict[str, Callable[[], MLModel]] = {}
        self._instances: dict[str, MLModel] = {}

    def register(self, name: str, factory: Callable[[], MLModel]) -> None:
        self._factories[name] = factory

    def get(self, name: str) -> MLModel:
        if name not in self._instances:
            if name not in self._factories:
                raise KeyError(f"Unknown model: {name}")
            model = self._factories[name]()
            model.load()
            self._instances[name] = model
        return self._instances[name]

    def unload(self, name: str) -> None:
        if name in self._instances:
            self._instances[name].unload()
            del self._instances[name]

    def unload_all(self) -> None:
        for name in list(self._instances):
            self.unload(name)

    def predict(self, name: str, image_path: Path) -> dict:
        return self.get(name).predict(image_path)


registry = ModelRegistry()
