import logging
from pathlib import Path

import numpy as np
from numpy.linalg import norm

from backend.db.connection import get_db

logger = logging.getLogger(__name__)

CLUSTER_THRESHOLD = 0.6


def cosine_similarity(a: np.ndarray, b: np.ndarray) -> float:
    return float(np.dot(a, b) / (norm(a) * norm(b) + 1e-8))


def load_embedding(path: str) -> np.ndarray:
    return np.load(str(Path(path))).flatten()


async def cluster_faces(threshold: float = CLUSTER_THRESHOLD) -> dict:
    async with get_db() as db:
        cursor = await db.execute(
            "SELECT id, embedding_path FROM faces WHERE embedding_path IS NOT NULL ORDER BY id"
        )
        rows = await cursor.fetchall()

    if not rows:
        return {"clusters": 0, "faces": 0}

    face_ids = [r[0] for r in rows]
    embeddings = [load_embedding(r[1]) for r in rows]
    n = len(face_ids)

    assigned = [-1] * n
    cluster_id = 0
    cluster_map: dict[int, list[int]] = {}

    for i in range(n):
        if assigned[i] != -1:
            continue
        assigned[i] = cluster_id
        cluster_map[cluster_id] = [face_ids[i]]
        for j in range(i + 1, n):
            if assigned[j] == -1 and cosine_similarity(embeddings[i], embeddings[j]) >= threshold:
                assigned[j] = cluster_id
                cluster_map[cluster_id].append(face_ids[j])
        cluster_id += 1

    async with get_db() as db:
        for face_id, cid in zip(face_ids, assigned):
            await db.execute(
                "UPDATE faces SET cluster_id = ? WHERE id = ?",
                (cid, face_id),
            )
        await db.commit()

    logger.info("Clustered %d faces into %d clusters", n, cluster_id)
    return {"clusters": cluster_id, "faces": n}
