from pathlib import Path

import numpy as np
import torch
from PIL import Image

from backend.ml.base import MLModel


class CLIPEncoder(MLModel):
    def __init__(self, model_name: str = "openai/clip-vit-base-patch32", device: str | None = None) -> None:
        super().__init__(name="clip", device=device)
        self.model_name = model_name
        self._model = None
        self._processor = None

    def load(self) -> None:
        from transformers import CLIPModel, CLIPProcessor

        self._model = CLIPModel.from_pretrained(self.model_name).to(self.device)
        self._model.eval()
        self._processor = CLIPProcessor.from_pretrained(self.model_name)
        self._loaded = True

    def unload(self) -> None:
        self._model = None
        self._processor = None
        self._loaded = False

    def predict(self, image_path: Path) -> dict:
        image = Image.open(image_path).convert("RGB")
        inputs = self._processor(images=image, return_tensors="pt").to(self.device)
        with torch.no_grad():
            embedding = self._model.get_image_features(**inputs)
        embedding_norm = embedding / embedding.norm(dim=-1, keepdim=True)
        return {
            "model": "clip-vit-base-patch32",
            "embedding": embedding_norm.cpu().numpy().astype(np.float32).tobytes(),
        }

    def encode_text(self, text: str) -> np.ndarray:
        inputs = self._processor(text=[text], return_tensors="pt", padding=True).to(self.device)
        with torch.no_grad():
            embedding = self._model.get_text_features(**inputs)
        embedding_norm = embedding / embedding.norm(dim=-1, keepdim=True)
        return embedding_norm.cpu().numpy().astype(np.float32)
