package com.pixelvault.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PhotoDao {

    @Query("SELECT * FROM photos ORDER BY created_at DESC")
    suspend fun getAllPhotos(): List<PhotoEntity>

    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getPhotoById(id: Long): PhotoEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(photos: List<PhotoEntity>)

    @Query("SELECT COUNT(*) FROM photos")
    suspend fun count(): Int

    @Query("SELECT MAX(synced_at) FROM photos")
    suspend fun lastSyncTime(): String?

    @Query("SELECT * FROM photos WHERE id NOT IN (SELECT DISTINCT photo_id FROM tags)")
    suspend fun getPhotosWithoutTags(): List<PhotoEntity>

    @Query("SELECT * FROM photos WHERE is_processed = 0")
    suspend fun getUnprocessedPhotos(): List<PhotoEntity>

    @Query("""
        SELECT DISTINCT p.* FROM photos p
        INNER JOIN tags t ON p.id = t.photo_id
        WHERE t.label LIKE '%' || :query || '%'
        ORDER BY t.confidence DESC
    """)
    suspend fun searchByTags(query: String): List<PhotoEntity>

    @Query("""
        SELECT p.* FROM photos p
        INNER JOIN faces f ON p.id = f.photo_id
        WHERE f.cluster_id = :clusterId
        GROUP BY p.id
    """)
    suspend fun getPhotosByCluster(clusterId: Long): List<PhotoEntity>

    @Query("UPDATE photos SET is_processed = 1 WHERE id = :id")
    suspend fun markProcessed(id: Long)

    @Query("UPDATE photos SET scene_label = :label, scene_confidence = :confidence WHERE id = :id")
    suspend fun updateSceneLabel(id: Long, label: String?, confidence: Double?)

    @Query("UPDATE photos SET food_label = :label WHERE id = :id")
    suspend fun updateFoodLabel(id: Long, label: String?)

    @Query("UPDATE photos SET face_count = :count WHERE id = :id")
    suspend fun updateFaceCount(id: Long, count: Int)

    @Query("SELECT * FROM photos WHERE hash = :hash LIMIT 1")
    suspend fun getByHash(hash: String): PhotoEntity?
}
