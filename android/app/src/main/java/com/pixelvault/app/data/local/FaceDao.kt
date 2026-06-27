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

    @Query("SELECT * FROM faces WHERE cluster_id = :clusterId")
    suspend fun getFacesByCluster(clusterId: Long): List<FaceEntity>

    @Query("SELECT DISTINCT photo_id FROM faces WHERE cluster_id = :clusterId")
    suspend fun getPhotoIdsByCluster(clusterId: Long): List<Long>

    @Query("SELECT * FROM faces WHERE cluster_id IS NULL")
    suspend fun getUnclusteredFaces(): List<FaceEntity>

    @Query("DELETE FROM faces WHERE photo_id = :photoId")
    suspend fun deleteFacesForPhoto(photoId: Long)

    @Query("UPDATE faces SET cluster_id = :clusterId WHERE id = :faceId")
    suspend fun assignFaceToCluster(faceId: Long, clusterId: Long)

    @Query("UPDATE faces SET cluster_id = :clusterId WHERE id IN (:faceIds)")
    suspend fun assignFacesToCluster(faceIds: List<Long>, clusterId: Long)

    @Query("SELECT * FROM faces")
    suspend fun getAllFaces(): List<FaceEntity>
}
