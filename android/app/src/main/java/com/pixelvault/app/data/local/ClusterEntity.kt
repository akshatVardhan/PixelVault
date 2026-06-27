package com.pixelvault.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clusters",
    foreignKeys = [
        ForeignKey(
            entity = FaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["representative_face_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("representative_face_id")]
)
data class ClusterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "face_count")
    val faceCount: Int = 0,
    @ColumnInfo(name = "representative_face_id")
    val representativeFaceId: Long? = null
)
