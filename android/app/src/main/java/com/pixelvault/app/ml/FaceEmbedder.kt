package com.pixelvault.app.ml

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceEmbedder @Inject constructor(
    private val modelLoader: ModelLoader
) {
    private val inputSize = 112

    suspend fun embed(faceBitmap: Bitmap): FloatArray = withContext(Dispatchers.Default) {
        val interpreter = modelLoader.load("mobilefacenet.tflite")
        val input = preprocess(faceBitmap)
        val output = Array(1) { FloatArray(128) }
        interpreter.run(input, output)
        output[0]
    }

    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val buffer = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
        }
        val pixels = IntArray(inputSize * inputSize)
        resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            buffer.putFloat((r - 0.5f) / 0.5f)
            buffer.putFloat((g - 0.5f) / 0.5f)
            buffer.putFloat((b - 0.5f) / 0.5f)
        }
        buffer.rewind()
        return buffer
    }
}
