from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel

from backend.api.auth import get_current_user
from backend.db.connection import get_db
from backend.ml.face.cluster import cluster_faces

router = APIRouter(dependencies=[Depends(get_current_user)])


class ClusterNameUpdate(BaseModel):
    name: str


@router.get("/faces/clusters")
async def list_clusters():
    async with get_db() as db:
        cursor = await db.execute(
            """
            SELECT c.id, c.name, c.created_at, COUNT(f.id) AS face_count
            FROM clusters c
            LEFT JOIN faces f ON f.cluster_id = c.id
            GROUP BY c.id
            ORDER BY c.id
            """
        )
        rows = await cursor.fetchall()
    return {
        "clusters": [
            {"id": r[0], "name": r[1], "created_at": r[2], "face_count": r[3]}
            for r in rows
        ]
    }


@router.get("/faces/clusters/{cluster_id}/photos")
async def get_cluster_photos(cluster_id: int):
    async with get_db() as db:
        cursor = await db.execute(
            """
            SELECT p.id, p.filename, p.path, p.created_at
            FROM photos p
            JOIN faces f ON f.photo_id = p.id
            WHERE f.cluster_id = ?
            GROUP BY p.id
            ORDER BY p.created_at DESC
            """,
            (cluster_id,),
        )
        rows = await cursor.fetchall()
    if not rows:
        raise HTTPException(status_code=404, detail="Cluster not found or empty")
    return {
        "cluster_id": cluster_id,
        "photos": [
            {"id": r[0], "filename": r[1], "path": r[2], "created_at": r[3]}
            for r in rows
        ],
    }


@router.put("/faces/clusters/{cluster_id}/name")
async def rename_cluster(cluster_id: int, body: ClusterNameUpdate):
    async with get_db() as db:
        cursor = await db.execute("SELECT id FROM clusters WHERE id = ?", (cluster_id,))
        row = await cursor.fetchone()
        if row is None:
            raise HTTPException(status_code=404, detail="Cluster not found")
        await db.execute("UPDATE clusters SET name = ? WHERE id = ?", (body.name, cluster_id))
        await db.commit()
    return {"status": "updated", "cluster_id": cluster_id, "name": body.name}


@router.post("/faces/cluster")
async def run_clustering():
    result = await cluster_faces()
    return result
