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

    @Query("DELETE FROM tags WHERE photo_id = :photoId")
    suspend fun deleteTagsForPhoto(photoId: Long)

    @Query("SELECT * FROM tags WHERE photo_id = :photoId AND type = :type")
    suspend fun getTagsByType(photoId: Long, type: String): List<TagEntity>

    @Query("SELECT DISTINCT photo_id FROM tags WHERE label LIKE '%' || :query || '%'")
    suspend fun searchPhotoIdsByLabel(query: String): List<Long>

    @Query("SELECT DISTINCT photo_id FROM tags WHERE label LIKE '%' || :query || '%' AND type = 'scene'")
    suspend fun searchPhotoIdsByScene(query: String): List<Long>
}
