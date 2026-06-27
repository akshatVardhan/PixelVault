package com.pixelvault.app.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.room.RoomDatabase
import com.pixelvault.app.data.local.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MLPipelineService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sceneDetector: SceneDetector,
    private val foodClassifier: FoodClassifier,
    private val faceDetector: FaceDetector,
    private val faceEmbedder: FaceEmbedder,
    private val db: AppDatabase
) {
    private val photoDao get() = db.photoDao()
    private val tagDao get() = db.tagDao()
    private val faceDao get() = db.faceDao()

    suspend fun processOnePhoto(photo: PhotoEntity) = withContext(Dispatchers.Default) {
        val bitmap = loadBitmap(photo.path) ?: return@withContext

        val sceneDetections = sceneDetector.detect(bitmap)
        val foodClassifications = foodClassifier.classify(bitmap)
        val faceBounds = faceDetector.detect(bitmap)

        val dbResults = DbResults(
            sceneTags = sceneDetections.take(5).map {
                TagEntity(photoId = photo.id, type = "tag", label = it.label, confidence = it.confidence.toDouble())
            },
            sceneLabel = sceneDetections.firstOrNull()?.label,
            sceneConfidence = sceneDetections.firstOrNull()?.confidence?.toDouble(),
            foodTags = foodClassifications.filter { it.confidence > 0.3f }.map {
                TagEntity(photoId = photo.id, type = "food", label = it.label, confidence = it.confidence.toDouble())
            },
            foodLabel = foodClassifications.firstOrNull()?.let { if (it.confidence > 0.3f) it.label else null },
            faceEntities = faceBounds.mapNotNull { fb ->
                val crop = cropBitmap(bitmap, fb.left, fb.top, fb.right, fb.bottom) ?: return@mapNotNull null
                val embedding = faceEmbedder.embed(crop)
                FaceEntity(photoId = photo.id, embedding = packEmbedding(embedding), boundingBox = "${fb.left},${fb.top},${fb.right},${fb.bottom}")
            }
        )

        db.withTransaction {
            tagDao.deleteTagsForPhoto(photo.id)
            faceDao.deleteFacesForPhoto(photo.id)
            tagDao.insertAll(dbResults.sceneTags + dbResults.foodTags)
            faceDao.insertAll(dbResults.faceEntities)
            dbResults.sceneLabel?.let { photoDao.updateSceneLabel(photo.id, it, dbResults.sceneConfidence) }
            dbResults.foodLabel?.let { photoDao.updateFoodLabel(photo.id, it) }
            photoDao.updateFaceCount(photo.id, dbResults.faceEntities.size)
            photoDao.markProcessed(photo.id)
        }

        bitmap.recycle()
    }

    private data class DbResults(
        val sceneTags: List<TagEntity>,
        val sceneLabel: String?,
        val sceneConfidence: Double?,
        val foodTags: List<TagEntity>,
        val foodLabel: String?,
        val faceEntities: List<FaceEntity>
    )

    private fun loadBitmap(path: String): Bitmap? {
        return try {
            val uri = Uri.parse(path)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val opts = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, opts)
            inputStream.close()

            val maxDimension = 1280
            val scale = maxOf(opts.outWidth, opts.outHeight) / maxDimension.toFloat()
            val sampleSize = if (scale > 1f) (1 until 32).firstOrNull { scale <= it } ?: 1 else 1

            val finalOpts = BitmapFactory.Options().apply {
                inSampleSize = maxOf(sampleSize, 1)
            }
            val inputStream2 = context.contentResolver.openInputStream(uri) ?: return null
            BitmapFactory.decodeStream(inputStream2, null, finalOpts)
        } catch (_: Exception) {
            null
        }
    }

    private fun cropBitmap(bitmap: Bitmap, left: Float, top: Float, right: Float, bottom: Float): Bitmap? {
        val bw = bitmap.width
        val bh = bitmap.height
        val x = (left * bw).toInt().coerceIn(0, bw)
        val y = (top * bh).toInt().coerceIn(0, bh)
        val w = ((right - left) * bw).toInt().coerceIn(1, bw - x)
        val h = ((bottom - top) * bh).toInt().coerceIn(1, bh - y)
        return try {
            Bitmap.createBitmap(bitmap, x, y, w, h)
        } catch (_: Exception) {
            null
        }
    }

    private fun packEmbedding(embedding: FloatArray): ByteArray {
        val bb = ByteBuffer.allocate(embedding.size * 4).apply { order(ByteOrder.nativeOrder()) }
        embedding.forEach { bb.putFloat(it) }
        return bb.array()
    }
}
