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
}
