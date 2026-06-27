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

    @Query("SELECT * FROM photos WHERE hash = :hash LIMIT 1")
    suspend fun getByHash(hash: String): PhotoEntity?

    @Query("UPDATE photos SET scene_label = :label WHERE id = :photoId")
    suspend fun updateSceneLabel(photoId: Long, label: String)

    @Query("UPDATE photos SET is_processed = 1 WHERE id = :photoId")
    suspend fun markProcessed(photoId: Long)

    @Query("SELECT * FROM photos WHERE SUBSTR(created_at, 6, 5) = :monthDay")
    suspend fun getPhotosOnThisDay(monthDay: String): List<PhotoEntity>
}
