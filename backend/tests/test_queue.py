import asyncio
from pathlib import Path

import pytest

from backend.ml.queue import Job, MLQueue


@pytest.mark.asyncio
async def test_enqueue_and_process():
    results = []
    def handler(job: Job):
        results.append(job.photo_id)

    q = MLQueue(num_workers=1)
    q.set_handler(handler)
    q.start()
    await q.enqueue(Job(priority=0, photo_id=1, image_path=Path("/tmp/a.jpg")))
    await asyncio.sleep(0.3)
    assert results == [1]
    await q.stop()


@pytest.mark.asyncio
async def test_priority_order():
    results = []
    def handler(job: Job):
        results.append(job.photo_id)

    q = MLQueue(num_workers=1)
    q.set_handler(handler)
    q.start()
    await q.enqueue(Job(priority=2, photo_id=2, image_path=Path("/tmp/b.jpg")))
    await q.enqueue(Job(priority=1, photo_id=1, image_path=Path("/tmp/a.jpg")))
    await asyncio.sleep(0.3)
    assert results == [1, 2]
    await q.stop()


@pytest.mark.asyncio
async def test_qsize():
    q = MLQueue(num_workers=1)
    assert q.qsize == 0
    await q.enqueue(Job(priority=0, photo_id=1, image_path=Path("/tmp/a.jpg")))
    assert q.qsize >= 1
    await q.stop()


@pytest.mark.asyncio
async def test_start_stop_idempotent():
    q = MLQueue(num_workers=1)
    q.start()
    q.start()
    assert q._running
    await q.stop()
    await q.stop()
    assert not q._running


@pytest.mark.asyncio
async def test_handler_error_does_not_crash_worker():
    errors = []
    def handler(job: Job):
        errors.append(job.photo_id)
        raise ValueError("boom")

    q = MLQueue(num_workers=1)
    q.set_handler(handler)
    q.start()
    await q.enqueue(Job(priority=0, photo_id=1, image_path=Path("/tmp/a.jpg")))
    await asyncio.sleep(0.3)
    await q.enqueue(Job(priority=0, photo_id=2, image_path=Path("/tmp/b.jpg")))
    await asyncio.sleep(0.3)
    assert errors == [1, 2]
    await q.stop()


@pytest.mark.asyncio
async def test_multiple_workers():
    results = []
    def handler(job: Job):
        results.append(job.photo_id)

    q = MLQueue(num_workers=3)
    q.set_handler(handler)
    q.start()
    for i in range(6):
        await q.enqueue(Job(priority=0, photo_id=i, image_path=Path(f"/tmp/{i}.jpg")))
    await asyncio.sleep(0.5)
    assert len(results) == 6
    await q.stop()
