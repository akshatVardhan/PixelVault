package com.pixelvault.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey val id: Long,
    val filename: String,
    val hash: String,
    val size: Long,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "synced_at") val syncedAt: String?,
    val path: String
)
