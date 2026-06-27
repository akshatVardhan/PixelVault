package com.pixelvault.app.ml

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val delegateSelector: DelegateSelector
) {
    private val cache = ConcurrentHashMap<String, Interpreter>()

    suspend fun load(modelPath: String): Interpreter {
        return cache.getOrPut(modelPath) {
            val buffer = loadModelFile(modelPath)
            val hash = md5(buffer)
            val delegate = delegateSelector.select(hash)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                when (delegate) {
                    DelegateSelector.DelegateType.NNAPI -> setDelegate(org.tensorflow.lite.nnapi.NnApiDelegate())
                    DelegateSelector.DelegateType.GPU -> setDelegate(org.tensorflow.lite.gpu.GpuDelegateFactory().create())
                    DelegateSelector.DelegateType.CPU -> { }
                }
            }
            Interpreter(buffer, options)
        }
    }

    fun unload(modelPath: String) {
        cache.remove(modelPath)?.close()
    }

    fun closeAll() {
        cache.values.forEach { it.close() }
        cache.clear()
    }

    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        val fd = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fd.fileDescriptor)
        val channel = inputStream.channel
        val startOffset = fd.startOffset
        val declaredLength = fd.declaredLength
        return channel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun md5(buffer: MappedByteBuffer): String {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = ByteArray(buffer.remaining())
        buffer.duplicate().get(bytes)
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
