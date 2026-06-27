package com.pixelvault.app.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Size
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SceneDetector @Inject constructor(
    private val context: Context,
    private val modelLoader: ModelLoader
) {
    private val labels: List<String> by lazy {
        val reader = BufferedReader(InputStreamReader(context.assets.open("coco_labels.txt")))
        reader.readLines()
    }

    fun detect(bitmap: Bitmap): List<Detection> {
        val interpreter = modelLoader.load("yolov8n_int8.tflite")
        val inputSize = Size(640, 640)
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize.width, inputSize.height, true)
        val input = preprocess(resized)
        val output = Array(1) { Array(84) { FloatArray(8400) } }
        interpreter.run(input, output)
        return postprocess(output[0])
    }

    private fun preprocess(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val w = bitmap.width
        val h = bitmap.height
        val intValues = IntArray(w * h)
        bitmap.getPixels(intValues, 0, w, 0, 0, w, h)
        val input = Array(1) { Array(3) { Array(h) { FloatArray(w) } } }
        for (y in 0 until h) {
            for (x in 0 until w) {
                val pixel = intValues[y * w + x]
                input[0][0][y][x] = ((pixel shr 16) and 0xFF) / 255.0f
                input[0][1][y][x] = ((pixel shr 8) and 0xFF) / 255.0f
                input[0][2][y][x] = (pixel and 0xFF) / 255.0f
            }
        }
        return input
    }

    private fun postprocess(output: Array<FloatArray>): List<Detection> {
        val results = mutableListOf<Detection>()
        val rows = output.size
        val cols = output[0].size
        for (i in 0 until cols) {
            val maxConf = (4 until rows).maxOfOrNull { output[it][i] } ?: 0f
            if (maxConf < 0.5f) continue
            val labelIdx = (4 until rows).indexOfFirst { output[it][i] == maxConf }
            if (labelIdx < 0 || labelIdx >= labels.size) continue
            val cx = output[0][i] / 640f
            val cy = output[1][i] / 640f
            val w = output[2][i] / 640f
            val h = output[3][i] / 640f
            results.add(
                Detection(
                    label = labels[labelIdx],
                    confidence = maxConf,
                    boundingBox = RectF(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2)
                )
            )
        }
        return results
    }
}
