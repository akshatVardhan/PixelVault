package com.pixelvault.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ClusterDao {
    @Insert
    suspend fun insert(cluster: ClusterEntity): Long

    @Query("SELECT * FROM clusters ORDER BY created_at DESC")
    suspend fun getAllClusters(): List<ClusterEntity>

    @Query("SELECT * FROM clusters WHERE id = :id")
    suspend fun getClusterById(id: Long): ClusterEntity?

    @Query("UPDATE clusters SET name = :name WHERE id = :id")
    suspend fun renameCluster(id: Long, name: String?)

    @Query("UPDATE clusters SET representative_face_id = :faceId WHERE id = :id")
    suspend fun updateRepresentative(id: Long, faceId: Long?)

    @Query("UPDATE clusters SET face_count = (SELECT COUNT(*) FROM faces WHERE cluster_id = :id) WHERE id = :id")
    suspend fun recountFaces(id: Long)
}
