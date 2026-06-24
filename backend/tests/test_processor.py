from pathlib import Path
from unittest.mock import AsyncMock, MagicMock, patch

import pytest

from backend.ml.processor import MLProcessor
from backend.ml.queue import MLQueue


@pytest.mark.asyncio
async def test_processor_enqueue_and_process(tmp_path):
    ml_queue = MLQueue(num_workers=1)
    processor = MLProcessor(ml_queue, num_workers=1)

    img = tmp_path / "test.jpg"
    img.write_bytes(b"fake")

    with (
        patch("backend.ml.processor.registry.predict") as mock_predict,
        patch("backend.ml.processor.MLProcessor._store_result") as mock_store,
    ):
        mock_predict.return_value = {"model": "scene", "scene_labels": ["person"]}
        await processor.enqueue(photo_id=1, image_path=img, model_names=["scene"])
        import asyncio
        await asyncio.sleep(0.3)
        mock_predict.assert_called_once_with("scene", img)
        mock_store.assert_called_once_with(1, "scene", {"model": "scene", "scene_labels": ["person"]})

    await processor.shutdown()


@pytest.mark.asyncio
async def test_processor_unknown_model_skips(tmp_path):
    ml_queue = MLQueue(num_workers=1)
    processor = MLProcessor(ml_queue, num_workers=1)

    img = tmp_path / "test.jpg"
    img.write_bytes(b"fake")

    with patch("backend.ml.processor.registry.predict") as mock_predict:
        mock_predict.side_effect = KeyError("unknown")
        await processor.enqueue(photo_id=1, image_path=img, model_names=["nonexistent"])
        import asyncio
        await asyncio.sleep(0.3)

    await processor.shutdown()


def test_store_clip_result():
    mock_db = AsyncMock()
    mock_cm = MagicMock()
    mock_cm.__aenter__.return_value = mock_db
    with patch("backend.ml.processor.get_db", return_value=mock_cm):
        result = {"embedding": b"\x00\x01\x02\x03"}
        MLProcessor._store_clip_result(1, result)
        mock_db.execute.assert_called_once_with(
            "UPDATE photos SET clip_embedding = ? WHERE id = ?",
            (b"\x00\x01\x02\x03", 1),
        )
        mock_db.commit.assert_called_once()


def test_store_clip_result_no_embedding():
    with patch("backend.ml.processor.get_db") as mock_get_db:
        MLProcessor._store_clip_result(1, {})
        mock_get_db.assert_not_called()
