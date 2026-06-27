package com.pixelvault.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE photos ADD COLUMN scene_label TEXT")
                db.execSQL("ALTER TABLE photos ADD COLUMN scene_confidence REAL")
                db.execSQL("ALTER TABLE photos ADD COLUMN food_label TEXT")
                db.execSQL("ALTER TABLE photos ADD COLUMN face_count INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE photos ADD COLUMN is_processed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS faces (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        photo_id INTEGER NOT NULL,
                        embedding BLOB,
                        bounding_box TEXT,
                        cluster_id INTEGER,
                        thumbnail_path TEXT,
                        FOREIGN KEY (photo_id) REFERENCES photos(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_faces_photo_id ON faces(photo_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_faces_cluster_id ON faces(cluster_id)")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS clusters (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT,
                        created_at TEXT NOT NULL,
                        face_count INTEGER NOT NULL DEFAULT 0,
                        representative_face_id INTEGER,
                        FOREIGN KEY (representative_face_id) REFERENCES faces(id) ON DELETE SET NULL
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_clusters_representative_face_id ON clusters(representative_face_id)")
            }
        }
    }
}
