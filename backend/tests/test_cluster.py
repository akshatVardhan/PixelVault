import numpy as np
import pytest

from backend.ml.face.cluster import cosine_similarity


def test_cluster_separate_faces():
    emb1 = np.array([1.0, 0.0, 0.0])
    emb2 = np.array([0.0, 1.0, 0.0])
    assert cosine_similarity(emb1, emb2) < 0.6


def test_cluster_same_faces():
    emb1 = np.array([1.0, 0.0, 0.0])
    emb2 = np.array([0.9, 0.1, 0.0])
    assert cosine_similarity(emb1, emb2) >= 0.6
