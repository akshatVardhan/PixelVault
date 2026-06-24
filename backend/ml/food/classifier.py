from pathlib import Path

import torch
from PIL import Image
from torchvision import transforms as T

from backend.ml.base import MLModel

FOOD_LABELS = [
    "apple_pie", "bibimbap", "caesar_salad", "donut", "fried_rice",
    "hamburger", "ice_cream", "omelette", "pizza", "ramen",
    "sashimi", "sushi", "taco", "waffle",
]


class FoodClassifier(MLModel):
    def __init__(self, device: str | None = None) -> None:
        super().__init__(name="food", device=device)
        self._model = None
        self._transform = T.Compose([
            T.Resize(256),
            T.CenterCrop(224),
            T.ToTensor(),
            T.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
        ])

    def load(self) -> None:
        from torchvision.models import efficientnet_v2_s, EfficientNet_V2_S_Weights

        self._model = efficientnet_v2_s(weights=EfficientNet_V2_S_Weights.DEFAULT)
        self._model.eval()
        self._model.to(self.device)
        self._loaded = True

    def unload(self) -> None:
        self._model = None
        self._loaded = False

    def predict(self, image_path: Path) -> dict:
        image = Image.open(image_path).convert("RGB")
        tensor = self._transform(image).unsqueeze(0).to(self.device)
        with torch.no_grad():
            logits = self._model(tensor)
        probs = torch.nn.functional.softmax(logits[0], dim=0)
        top_idx = probs.argmax().item()

        label = FOOD_LABELS[top_idx] if top_idx < len(FOOD_LABELS) else f"class_{top_idx}"
        return {
            "model": "efficientnet_v2_s",
            "food_label": label,
            "confidence": round(probs[top_idx].item(), 4),
        }
