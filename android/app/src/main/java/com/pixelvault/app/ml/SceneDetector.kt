package com.pixelvault.app.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SceneDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelLoader: ModelLoader
) {
    private val labels: List<String> by lazy {
        val reader = BufferedReader(InputStreamReader(context.assets.open("coco_labels.txt")))
        reader.readLines()
    }
    private val inputSize = 640
    private val confThreshold = 0.25f
    private val iouThreshold = 0.45f

    suspend fun detect(bitmap: Bitmap): List<Detection> = withContext(Dispatchers.Default) {
        val interpreter = modelLoader.load("yolov8n_int8.tflite")
        val modelInput = preprocess(bitmap)
        val output = Array(1) { Array(84) { ByteArray(8400) } }
        interpreter.run(modelInput, output)
        postprocess(output[0])
    }

    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val buffer = ByteBuffer.allocateDirect(inputSize * inputSize * 3).apply {
            order(ByteOrder.nativeOrder())
        }
        val pixels = IntArray(inputSize * inputSize)
        resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
        for (pixel in pixels) {
            buffer.put(((pixel shr 16) and 0xFF).toByte())
            buffer.put(((pixel shr 8) and 0xFF).toByte())
            buffer.put((pixel and 0xFF).toByte())
        }
        buffer.rewind()
        return buffer
    }

    private fun postprocess(output: Array<ByteArray>): List<Detection> {
        val numClasses = 80
        val numAnchors = 8400
        val boxes = ArrayList<FloatArray>()
        val scores = ArrayList<FloatArray>()

        for (i in 0 until numAnchors) {
            var maxScore = 0f
            var maxClass = -1
            for (c in 0 until numClasses) {
                val score = (output[4 + c][i].toInt() and 0xFF) / 255.0f
                if (score > maxScore) {
                    maxScore = score
                    maxClass = c
                }
            }
            if (maxScore > confThreshold && maxClass >= 0) {
                val cx = (output[0][i].toInt() and 0xFF) / 255.0f
                val cy = (output[1][i].toInt() and 0xFF) / 255.0f
                val w = (output[2][i].toInt() and 0xFF) / 255.0f
                val h = (output[3][i].toInt() and 0xFF) / 255.0f
                val x1 = (cx - w / 2f).coerceIn(0f, 1f)
                val y1 = (cy - h / 2f).coerceIn(0f, 1f)
                val x2 = (cx + w / 2f).coerceIn(0f, 1f)
                val y2 = (cy + h / 2f).coerceIn(0f, 1f)
                boxes.add(floatArrayOf(x1, y1, x2, y2))
                scores.add(floatArrayOf(maxScore.toFloat(), maxClass.toFloat()))
            }
        }

        val detections = ArrayList<Detection>()
        val kept = nms(boxes, scores)
        for (idx in kept) {
            val box = boxes[idx]
            val score = scores[idx][0]
            val classId = scores[idx][1].toInt()
            detections.add(
                Detection(
                    label = labels.getOrElse(classId) { "unknown" },
                    confidence = score,
                    boundingBox = RectF(box[0], box[1], box[2], box[3])
                )
            )
        }
        return detections.sortedByDescending { it.confidence }
    }

    private fun nms(boxes: List<FloatArray>, scores: List<FloatArray>): List<Int> {
        val sorted = scores.withIndex().sortedByDescending { it.value[0] }.map { it.index }
        val kept = ArrayList<Int>()
        for (i in sorted) {
            var keep = true
            for (j in kept) {
                if (iou(boxes[i], boxes[j]) > iouThreshold) {
                    keep = false
                    break
                }
            }
            if (keep) kept.add(i)
        }
        return kept
    }

    private fun iou(a: FloatArray, b: FloatArray): Float {
        val x1 = maxOf(a[0], b[0])
        val y1 = maxOf(a[1], b[1])
        val x2 = minOf(a[2], b[2])
        val y2 = minOf(a[3], b[3])
        val inter = maxOf(0f, x2 - x1) * maxOf(0f, y2 - y1)
        val areaA = (a[2] - a[0]) * (a[3] - a[1])
        val areaB = (b[2] - b[0]) * (b[3] - b[1])
        return inter / (areaA + areaB - inter)
    }
}
