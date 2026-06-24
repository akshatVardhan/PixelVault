package com.pixelvault.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TagDao {

    @Query("SELECT * FROM tags WHERE photo_id = :photoId")
    suspend fun getTagsForPhoto(photoId: Long): List<TagEntity>

    @Query("SELECT * FROM tags WHERE label LIKE :query OR label IN (:tags)")
    suspend fun searchByTags(query: String, tags: List<String>): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tags: List<TagEntity>)
}
