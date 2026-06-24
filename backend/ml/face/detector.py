from pathlib import Path

import numpy as np

from backend.config import settings
from backend.ml.base import MLModel


class FaceDetector(MLModel):
    def __init__(self, device: str | None = None) -> None:
        super().__init__(name="face", device=device)
        self._app = None
        self._ctx_id = 0 if (device and "cuda" in device) else -1

    def load(self) -> None:
        from insightface.app import FaceAnalysis

        self._app = FaceAnalysis(name="buffalo_l", root=settings.storage_path.parent / "models")
        self._app.prepare(ctx_id=self._ctx_id)
        self._loaded = True

    def unload(self) -> None:
        self._app = None
        self._loaded = False

    def predict(self, image_path: Path) -> dict:
        import cv2

        img = cv2.imread(str(image_path))
        if img is None:
            return {"faces": []}

        faces = self._app.get(img)
        results = []
        for face in faces:
            embedding = face.embedding.astype(np.float32)
            embedding_path = settings.storage_path / "embeddings" / f"{image_path.stem}_{face.face_id}.npy"
            embedding_path.parent.mkdir(parents=True, exist_ok=True)
            np.save(str(embedding_path), embedding)

            results.append({
                "bbox": face.bbox.tolist(),
                "embedding_path": str(embedding_path),
                "confidence": round(face.det_score, 4),
            })

        return {"model": "insightface_arcface", "faces": results}
