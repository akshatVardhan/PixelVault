package com.pixelvault.app.ml

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.pixelvault.app.data.local.FaceDao
import com.pixelvault.app.data.local.FaceEntity
import com.pixelvault.app.data.local.PhotoDao
import com.pixelvault.app.data.local.PhotoEntity
import com.pixelvault.app.data.local.TagDao
import com.pixelvault.app.data.local.TagEntity
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MLPipelineService @Inject constructor(
    private val sceneDetector: SceneDetector,
    private val foodClassifier: FoodClassifier,
    private val faceDetector: FaceDetector,
    private val faceEmbedder: FaceEmbedder,
    private val faceClusterer: FaceClusterer,
    private val photoDao: PhotoDao,
    private val faceDao: FaceDao,
    private val tagDao: TagDao,
    private val contentResolver: ContentResolver
) {
    suspend fun processOnePhoto(photo: PhotoEntity) {
        val bitmap = loadBitmap(photo.path) ?: return

        val sceneDetections = sceneDetector.detect(bitmap)
        val bestScene = sceneDetections.maxByOrNull { it.confidence }
        if (bestScene != null) {
            photoDao.updateSceneLabel(photo.id, bestScene.label)
            tagDao.insertAll(
                listOf(TagEntity(photoId = photo.id, label = bestScene.label, type = "scene"))
            )
        }

        val foodResults = foodClassifier.classify(bitmap)
        val bestFood = foodResults.firstOrNull()
        if (bestFood != null) {
            tagDao.insertAll(
                listOf(TagEntity(photoId = photo.id, label = bestFood.label, type = "food"))
            )
        }

        val faceBounds = faceDetector.detect(bitmap)
        if (faceBounds.isNotEmpty()) {
            val faces = faceBounds.map { bounds ->
                val crop = Bitmap.createBitmap(
                    bitmap,
                    bounds.left.toInt().coerceAtLeast(0),
                    bounds.top.toInt().coerceAtLeast(0),
                    (bounds.right - bounds.left).toInt().coerceAtMost(bitmap.width),
                    (bounds.bottom - bounds.top).toInt().coerceAtMost(bitmap.height)
                )
                val embedding = faceEmbedder.embed(crop)
                FaceEntity(
                    photoId = photo.id,
                    embedding = packEmbedding(embedding),
                    boundingBox = "${bounds.left},${bounds.top},${bounds.right},${bounds.bottom}"
                )
            }
            faceDao.insertAll(faces)
        }

        photoDao.markProcessed(photo.id)
        faceClusterer.clusterNewFaces()
    }

    private fun loadBitmap(path: String): Bitmap? {
        return try {
            val uri = Uri.parse(path)
            if (uri.scheme == "content") {
                val inputStream = contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStream)
            } else {
                BitmapFactory.decodeFile(path)
            }
        } catch (_: Exception) {
            BitmapFactory.decodeFile(path)
        }
    }

    private fun packEmbedding(embedding: FloatArray): ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)
        for (v in embedding) dos.writeFloat(v)
        return bos.toByteArray()
    }
}
