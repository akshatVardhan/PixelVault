from pathlib import Path

from backend.ml.base import MLModel


class YOLOSceneModel(MLModel):
    def __init__(self, model_path: str = "yolov8n.pt", device: str | None = None) -> None:
        super().__init__(name="scene", device=device)
        self.model_path = model_path
        self._model = None

    def load(self) -> None:
        from ultralytics import YOLO

        self._model = YOLO(self.model_path)
        self._loaded = True

    def unload(self) -> None:
        self._model = None
        self._loaded = False

    def predict(self, image_path: Path) -> dict:
        results = self._model(str(image_path))
        detections = []
        for r in results:
            for box in r.boxes:
                detections.append({
                    "label": r.names[int(box.cls.item())],
                    "confidence": round(box.conf.item(), 4),
                    "bbox": box.xyxy[0].tolist(),
                })
        return {"model": "yolov8n", "scene_labels": list({d["label"] for d in detections}), "detections": detections}
