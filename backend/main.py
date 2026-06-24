from contextlib import asynccontextmanager

from fastapi import FastAPI

import pathlib

from fastapi.staticfiles import StaticFiles

from backend.api.routes.faces import router as faces_router
from backend.api.routes.search import router as search_router
from backend.api.routes.sync import router as sync_router
from backend.dashboard.router import router as dashboard_router
from backend.config import settings
from backend.db.connection import init_db
from backend.ml.face.detector import FaceDetector
from backend.ml.clip.encoder import CLIPEncoder
from backend.ml.food.classifier import FoodClassifier
from backend.ml.loader import registry
from backend.ml.processor import MLProcessor
from backend.ml.queue import MLQueue
from backend.ml.scene.yolo import YOLOSceneModel


@asynccontextmanager
async def lifespan(app: FastAPI):
    await init_db()
    ml_queue = MLQueue(num_workers=settings.ml_num_workers)
    registry.register("scene", lambda: YOLOSceneModel(model_path=settings.yolo_model_path, device=settings.ml_device))
    registry.register("food", lambda: FoodClassifier(device=settings.ml_device))
    registry.register("face", lambda: FaceDetector(device=settings.ml_device))
    registry.register("clip", lambda: CLIPEncoder(device=settings.ml_device))
    app.state.ml_processor = MLProcessor(ml_queue, num_workers=settings.ml_num_workers)
    yield
    await app.state.ml_processor.shutdown()


app = FastAPI(title="PixelVault API", lifespan=lifespan)
app.include_router(sync_router, prefix="/api/v1")
app.include_router(faces_router, prefix="/api/v1")
app.include_router(search_router, prefix="/api/v1")
app.include_router(dashboard_router)
dashboard_static = pathlib.Path(__file__).parent / "dashboard" / "static"
app.mount("/dashboard/static", StaticFiles(directory=str(dashboard_static)), name="dashboard_static")


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "ok"}
