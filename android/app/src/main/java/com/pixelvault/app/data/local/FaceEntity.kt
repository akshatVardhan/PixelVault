package com.pixelvault.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "faces",
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photo_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("photo_id"), Index("cluster_id")]
)
data class FaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "photo_id")
    val photoId: Long,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val embedding: ByteArray? = null,
    @ColumnInfo(name = "bounding_box")
    val boundingBox: String? = null,
    @ColumnInfo(name = "cluster_id")
    val clusterId: Long? = null,
    @ColumnInfo(name = "thumbnail_path")
    val thumbnailPath: String? = null
)
