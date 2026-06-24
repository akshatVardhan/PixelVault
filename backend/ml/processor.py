import logging
from pathlib import Path

from backend.db.connection import get_db
from backend.ml.loader import registry
from backend.ml.queue import Job, MLQueue

logger = logging.getLogger(__name__)


class MLProcessor:
    def __init__(self, ml_queue: MLQueue, num_workers: int = 1) -> None:
        self._queue = ml_queue
        self._queue.set_handler(self._handle_job)
        self._queue.start()

    @staticmethod
    def _handle_job(job: Job) -> None:
        model_names = job.model_names or ["scene", "food"]
        for name in model_names:
            try:
                result = registry.predict(name, job.image_path)
                MLProcessor._store_result(job.photo_id, name, result)
            except KeyError:
                logger.warning("Model '%s' not registered, skipping", name)
            except Exception:
                logger.exception("Error running model '%s' on photo_id=%d", name, job.photo_id)

    @staticmethod
    def _store_result(photo_id: int, model_name: str, result: dict) -> None:
        label = result.get("food_label") or result.get("scene_labels", [None])[0]
        if label is None:
            return
        async def write():
            db = await get_db()
            await db.execute(
                "INSERT INTO tags (photo_id, type, label, confidence) VALUES (?, ?, ?, ?)",
                (photo_id, model_name, str(label), result.get("confidence")),
            )
            await db.commit()
        import asyncio
        asyncio.run(write())

    async def enqueue(self, photo_id: int, image_path: Path, model_names: list[str] | None = None, priority: int = 0) -> None:
        await self._queue.enqueue(Job(priority=priority, photo_id=photo_id, image_path=image_path, model_names=model_names))

    async def shutdown(self) -> None:
        await self._queue.stop()
        registry.unload_all()
