from abc import ABC, abstractmethod
from pathlib import Path

import torch


class MLModel(ABC):
    def __init__(self, name: str, device: str | None = None) -> None:
        self.name = name
        self.device = device or ("cuda" if torch.cuda.is_available() else "cpu")
        self._loaded = False

    @property
    def loaded(self) -> bool:
        return self._loaded

    @abstractmethod
    def load(self) -> None:
        ...

    @abstractmethod
    def unload(self) -> None:
        ...

    @abstractmethod
    def predict(self, image_path: Path) -> dict:
        ...
