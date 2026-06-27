package com.pixelvault.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FaceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(faces: List<FaceEntity>)

    @Query("DELETE FROM faces WHERE photo_id = :photoId")
    suspend fun deleteFacesForPhoto(photoId: Long)

    @Query("SELECT * FROM faces WHERE photo_id = :photoId")
    suspend fun getFacesForPhoto(photoId: Long): List<FaceEntity>

    @Query("SELECT * FROM faces")
    suspend fun getAllFaces(): List<FaceEntity>
}
