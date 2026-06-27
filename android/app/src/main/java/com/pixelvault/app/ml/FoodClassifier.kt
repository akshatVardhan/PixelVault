package com.pixelvault.app.ml

import android.content.Context
import android.graphics.Bitmap
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodClassifier @Inject constructor(
    private val context: Context,
    private val modelLoader: ModelLoader
) {
    private val labels: List<String> by lazy {
        val reader = BufferedReader(InputStreamReader(context.assets.open("imagenet_labels.txt")))
        reader.readLines()
    }

    fun classify(bitmap: Bitmap): List<Classification> {
        val interpreter = modelLoader.load("efficientnet_lite0_int8.tflite")
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val input = preprocess(resized)
        val output = Array(1) { FloatArray(1000) }
        interpreter.run(input, output)
        return postprocess(output[0])
    }

    private fun preprocess(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val intValues = IntArray(224 * 224)
        bitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)
        val input = Array(1) { Array(3) { Array(224) { FloatArray(224) } } }
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = intValues[y * 224 + x]
                input[0][0][y][x] = ((pixel shr 16) and 0xFF) / 127.5f - 1f
                input[0][1][y][x] = ((pixel shr 8) and 0xFF) / 127.5f - 1f
                input[0][2][y][x] = (pixel and 0xFF) / 127.5f - 1f
            }
        }
        return input
    }

    private fun postprocess(output: FloatArray): List<Classification> {
        val expSum = output.sumOf { kotlin.math.exp(it.toDouble()) }
        val probs = output.map { kotlin.math.exp(it.toDouble()) / expSum }
        val indexed = probs.withIndex()
            .sortedByDescending { it.value }
            .take(3)
        return indexed.map { (idx, prob) ->
            Classification(
                label = labels.getOrElse(idx) { "unknown" },
                confidence = prob.toFloat()
            )
        }
    }
}
