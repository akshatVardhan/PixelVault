package com.pixelvault.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clusters")
data class ClusterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "face_count") val faceCount: Int = 0,
    @ColumnInfo(name = "representative_embedding") val representativeEmbedding: ByteArray? = null
)
