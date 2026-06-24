import asyncio
import logging
from collections.abc import Callable
from dataclasses import dataclass, field
from pathlib import Path

logger = logging.getLogger(__name__)


@dataclass(order=True)
class Job:
    priority: int
    photo_id: int
    image_path: Path = field(compare=False)
    model_names: list[str] | None = field(default=None, compare=False)


class MLQueue:
    def __init__(self, num_workers: int = 1) -> None:
        self._queue: asyncio.PriorityQueue[Job] = asyncio.PriorityQueue()
        self._workers: list[asyncio.Task[None]] = []
        self._num_workers = num_workers
        self._handler: Callable[[Job], None] | None = None
        self._running = False

    def set_handler(self, handler: Callable[[Job], None]) -> None:
        self._handler = handler

    async def enqueue(self, job: Job) -> None:
        await self._queue.put(job)
        logger.info("Enqueued job for photo_id=%d (priority=%d)", job.photo_id, job.priority)

    async def _worker_loop(self, worker_id: int) -> None:
        logger.info("Worker %d started", worker_id)
        while self._running:
            try:
                job = await asyncio.wait_for(self._queue.get(), timeout=1.0)
            except TimeoutError:
                continue
            try:
                if self._handler:
                    self._handler(job)
                logger.info("Worker %d processed photo_id=%d", worker_id, job.photo_id)
            except Exception:
                logger.exception("Worker %d failed on photo_id=%d", worker_id, job.photo_id)
            finally:
                self._queue.task_done()

    def start(self) -> None:
        if self._running:
            return
        self._running = True
        self._workers = [
            asyncio.create_task(self._worker_loop(i))
            for i in range(self._num_workers)
        ]
        logger.info("MLQueue started with %d workers", self._num_workers)

    async def stop(self) -> None:
        self._running = False
        if self._workers:
            await asyncio.gather(*self._workers, return_exceptions=True)
            self._workers.clear()
        logger.info("MLQueue stopped")

    @property
    def qsize(self) -> int:
        return self._queue.qsize()
