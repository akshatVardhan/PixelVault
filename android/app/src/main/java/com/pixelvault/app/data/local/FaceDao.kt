package com.pixelvault.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FaceDao {
    @Insert
    suspend fun insertAll(faces: List<FaceEntity>)

    @Query("SELECT * FROM faces WHERE photo_id = :photoId")
    suspend fun getFacesForPhoto(photoId: Long): List<FaceEntity>

    @Query("SELECT * FROM faces WHERE id = :id")
    suspend fun getFaceById(id: Long): FaceEntity?

    @Query("UPDATE faces SET cluster_id = :clusterId WHERE id = :faceId")
    suspend fun assignCluster(faceId: Long, clusterId: Long?)

    @Query("SELECT * FROM faces WHERE cluster_id = :clusterId")
    suspend fun getFacesByCluster(clusterId: Long): List<FaceEntity>

    @Query("SELECT * FROM faces")
    suspend fun getAllFaces(): List<FaceEntity>

    @Query("DELETE FROM faces WHERE photo_id = :photoId")
    suspend fun deleteFacesForPhoto(photoId: Long)
}
