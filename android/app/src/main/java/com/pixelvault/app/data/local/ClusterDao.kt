package com.pixelvault.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClusterDao {
    @Query("SELECT * FROM clusters ORDER BY face_count DESC")
    suspend fun getAllClusters(): List<ClusterEntity>

    @Query("SELECT * FROM clusters WHERE id = :id")
    suspend fun getClusterById(id: Long): ClusterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cluster: ClusterEntity): Long

    @Query("UPDATE clusters SET name = :name WHERE id = :id")
    suspend fun renameCluster(id: Long, name: String)

    @Query("UPDATE clusters SET face_count = (SELECT COUNT(*) FROM faces WHERE cluster_id = :clusterId) WHERE id = :clusterId")
    suspend fun recountFaces(clusterId: Long)

    @Delete
    suspend fun delete(cluster: ClusterEntity)
}
