package com.pixelvault.app.ml

import android.content.Context
import android.content.res.AssetFileDescriptor
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelLoader @Inject constructor(
    private val context: Context,
    private val delegateSelector: DelegateSelector
) {
    private val cache = mutableMapOf<String, Interpreter>()

    fun load(modelName: String): Interpreter {
        return cache.getOrPut(modelName) {
            val buffer = loadModelFile(modelName)
            val delegate = delegateSelector.select()
            val options = Interpreter.Options().apply {
                when (delegate) {
                    DelegateSelector.DelegateType.NNAPI -> {
                        addDelegate(NnApiDelegate())
                    }
                    DelegateSelector.DelegateType.GPU -> {
                        try {
                            addDelegate(GpuDelegate())
                        } catch (_: Throwable) { }
                    }
                    DelegateSelector.DelegateType.CPU -> { }
                }
                setNumThreads(4)
            }
            Interpreter(buffer, options)
        }
    }

    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val afd: AssetFileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(afd.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = afd.startOffset
        val declaredLength = afd.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun close() {
        cache.values.forEach { it.close() }
        cache.clear()
    }
}
