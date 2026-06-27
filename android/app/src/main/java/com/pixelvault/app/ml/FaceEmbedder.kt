package com.pixelvault.app.ml

import android.graphics.Bitmap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceEmbedder @Inject constructor(
    private val modelLoader: ModelLoader
) {
    fun embed(bitmap: Bitmap): FloatArray {
        val interpreter = modelLoader.load("mobilefacenet.tflite")
        val resized = Bitmap.createScaledBitmap(bitmap, 112, 112, true)
        val input = preprocess(resized)
        val output = Array(1) { FloatArray(128) }
        interpreter.run(input, output)
        return normalize(output[0])
    }

    private fun preprocess(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val intValues = IntArray(112 * 112)
        bitmap.getPixels(intValues, 0, 112, 0, 0, 112, 112)
        val input = Array(1) { Array(3) { Array(112) { FloatArray(112) } } }
        for (y in 0 until 112) {
            for (x in 0 until 112) {
                val pixel = intValues[y * 112 + x]
                input[0][0][y][x] = (((pixel shr 16) and 0xFF) / 255.0f - 0.5f) * 2f
                input[0][1][y][x] = (((pixel shr 8) and 0xFF) / 255.0f - 0.5f) * 2f
                input[0][2][y][x] = ((pixel and 0xFF) / 255.0f - 0.5f) * 2f
            }
        }
        return input
    }

    private fun normalize(embedding: FloatArray): FloatArray {
        var norm = 0f
        for (v in embedding) norm += v * v
        norm = kotlin.math.sqrt(norm)
        if (norm > 0f) {
            for (i in embedding.indices) embedding[i] /= norm
        }
        return embedding
    }
}
