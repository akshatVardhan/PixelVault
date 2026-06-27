package com.pixelvault.app.ml

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodClassifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelLoader: ModelLoader
) {
    private val labels: List<String> by lazy {
        val reader = BufferedReader(InputStreamReader(context.assets.open("imagenet_labels.txt")))
        reader.readLines()
    }
    private val inputSize = 224

    suspend fun classify(bitmap: Bitmap): List<Classification> = withContext(Dispatchers.Default) {
        val interpreter = modelLoader.load("efficientnet_lite0_int8.tflite")
        val input = preprocess(bitmap)
        val output = Array(1) { ByteArray(1001) }
        interpreter.run(input, output)
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

    private fun postprocess(output: ByteArray): List<Classification> {
        val scores = FloatArray(output.size) { (output[it].toInt() and 0xFF) / 255.0f }
        val softmax = FloatArray(scores.size)
        var max = scores.max()
        var sum = 0f
        for (i in scores.indices) {
            softmax[i] = kotlin.math.exp(scores[i] - max)
            sum += softmax[i]
        }
        for (i in softmax.indices) softmax[i] /= sum
        return softmax.withIndex()
            .sortedByDescending { it.value }
            .take(3)
            .map { Classification(labels.getOrElse(it.index) { "unknown" }, it.value) }
    }
}
