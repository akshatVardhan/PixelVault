package com.pixelvault.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PhotoEntity::class, TagEntity::class, FaceEntity::class, ClusterEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun tagDao(): TagDao
    abstract fun faceDao(): FaceDao
    abstract fun clusterDao(): ClusterDao
}
