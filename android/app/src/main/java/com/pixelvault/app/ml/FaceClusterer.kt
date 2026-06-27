package com.pixelvault.app.ml

import com.pixelvault.app.data.local.ClusterDao
import com.pixelvault.app.data.local.ClusterEntity
import com.pixelvault.app.data.local.FaceDao
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceClusterer @Inject constructor(
    private val faceDao: FaceDao,
    private val clusterDao: ClusterDao
) {
    suspend fun clusterNewFaces() {
        val unclustered = faceDao.getUnclusteredFaces()
        if (unclustered.size < 5) return
        val allClusters = clusterDao.getAllClusters()
        for (face in unclustered) {
            val faceEmb = face.embedding?.let { unpackEmbedding(it) } ?: continue
            var bestClusterId: Long? = null
            var bestSim = 0.0
            for (cluster in allClusters) {
                val centroid = cluster.representativeEmbedding?.let { unpackEmbedding(it) } ?: continue
                val sim = cosineSimilarity(faceEmb, centroid)
                if (sim > bestSim && sim >= 0.6) {
                    bestSim = sim
                    bestClusterId = cluster.id
                }
            }
            if (bestClusterId != null) {
                faceDao.assignFaceToCluster(face.id, bestClusterId!!)
                recalculateCentroid(bestClusterId!!)
                clusterDao.recountFaces(bestClusterId!!)
            } else {
                val newId = clusterDao.insert(
                    ClusterEntity(
                        createdAt = currentTimestamp(),
                        faceCount = 1,
                        representativeEmbedding = face.embedding
                    )
                )
                faceDao.assignFaceToCluster(face.id, newId)
            }
        }
    }

    private suspend fun recalculateCentroid(clusterId: Long) {
        val faces = faceDao.getFacesByCluster(clusterId)
        if (faces.isEmpty()) return
        val embeddings = faces.mapNotNull { it.embedding?.let { emb -> unpackEmbedding(emb) } }
        if (embeddings.isEmpty()) return
        val dim = embeddings[0].size
        val centroid = DoubleArray(dim)
        for (emb in embeddings) {
            for (i in 0 until dim) centroid[i] += emb[i]
        }
        for (i in 0 until dim) centroid[i] /= embeddings.size
        val centroidFloat = FloatArray(dim) { centroid[it].toFloat() }
        var norm = 0f
        for (v in centroidFloat) norm += v * v
        norm = kotlin.math.sqrt(norm)
        if (norm > 0f) for (i in centroidFloat.indices) centroidFloat[i] /= norm
        clusterDao.insert(
            clusterDao.getClusterById(clusterId)!!.copy(
                representativeEmbedding = packEmbedding(centroidFloat)
            )
        )
    }

    private fun cosineSimilarity(a: FloatArray, b: DoubleArray): Double {
        var dot = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        return dot / (kotlin.math.sqrt(normA) * kotlin.math.sqrt(normB))
    }

    private fun unpackEmbedding(bytes: ByteArray): FloatArray {
        val dis = DataInputStream(ByteArrayInputStream(bytes))
        return FloatArray(bytes.size / 4) { dis.readFloat() }
    }

    private fun packEmbedding(embedding: FloatArray): ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)
        for (v in embedding) dos.writeFloat(v)
        return bos.toByteArray()
    }

    private fun currentTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
}
