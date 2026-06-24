package com.pixelvault.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tags",
    foreignKeys = [ForeignKey(
        entity = PhotoEntity::class,
        parentColumns = ["id"],
        childColumns = ["photo_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("photo_id"), Index("label")]
)
data class TagEntity(
    @PrimaryKey val id: Long = 0,
    @ColumnInfo(name = "photo_id") val photoId: Long,
    val type: String,
    val label: String,
    val confidence: Double?
)
