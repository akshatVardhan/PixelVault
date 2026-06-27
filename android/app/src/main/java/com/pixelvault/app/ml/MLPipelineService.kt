package com.pixelvault.app.ml

import android.graphics.BitmapFactory
import android.net.Uri
import com.pixelvault.app.data.local.AppDatabase
import com.pixelvault.app.data.local.FaceEntity
import com.pixelvault.app.data.local.PhotoEntity
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MLPipelineService @Inject constructor(
    private val sceneDetector: SceneDetector,
    private val foodClassifier: FoodClassifier,
    private val faceDetector: FaceDetector,
    private val faceEmbedder: FaceEmbedder,
    private val faceClusterer: FaceClusterer,
    private val db: AppDatabase
) {
    suspend fun processOnePhoto(photo: PhotoEntity) {
        val bitmap = try {
            val uri = Uri.parse(photo.path)
            val inputStream = android.content.ContentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (_: Exception) {
            BitmapFactory.decodeFile(photo.path)
        } ?: return

        db.withTransaction {
            val sceneDetections = sceneDetector.detect(bitmap)
            val bestScene = sceneDetections.maxByOrNull { it.confidence }
            if (bestScene != null) {
                db.photoDao().updateSceneLabel(photo.id, bestScene.label, bestScene.confidence.toDouble())
            }

            val foodResults = foodClassifier.classify(bitmap)
            val bestFood = foodResults.firstOrNull()
            if (bestFood != null) {
                db.photoDao().updateFoodLabel(photo.id, bestFood.label)
            }

            val faceBounds = faceDetector.detect(bitmap)
            db.photoDao().updateFaceCount(photo.id, faceBounds.size)

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
                db.faceDao().insertAll(faces)
            }

            db.photoDao().markProcessed(photo.id)
        }

        faceClusterer.clusterNewFaces()
    }

    private fun packEmbedding(embedding: FloatArray): ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)
        for (v in embedding) dos.writeFloat(v)
        return bos.toByteArray()
    }
}
