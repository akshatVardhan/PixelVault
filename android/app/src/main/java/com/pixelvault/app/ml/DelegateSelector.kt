package com.pixelvault.app.ml

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegateFactory
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

private val Context.blacklistStore by preferencesDataStore(name = "delegate_blacklist")

@Singleton
class DelegateSelector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    enum class DelegateType { NNAPI, GPU, CPU }

    private val modelsDir = File(context.filesDir, "delegate_probes").also { it.mkdirs() }

    suspend fun select(modelHash: String): DelegateType {
        val blacklist = context.blacklistStore.data.map { prefs ->
            prefs[stringSetPreferencesKey("blacklist")] ?: emptySet()
        }.first()

        for (type in listOf(DelegateType.NNAPI, DelegateType.GPU)) {
            if (modelHash in blacklist) continue
            val flagFile = File(modelsDir, "probe_${type.name}_$modelHash")
            try {
                flagFile.createNewFile()
                probeDelegate(type)
                flagFile.delete()
                return type
            } catch (_: Throwable) {
                flagFile.delete()
            }
        }
        return DelegateType.CPU
    }

    private fun probeDelegate(type: DelegateType) {
        val options = Interpreter.Options().apply {
            when (type) {
                DelegateType.NNAPI -> setDelegate(NnApiDelegate())
                DelegateType.GPU -> setDelegate(GpuDelegateFactory().create())
                DelegateType.CPU -> { /* no delegate */ }
            }
        }
        val input = ByteBuffer.allocateDirect(4).putFloat(1f).apply { rewind() }
        val output = Array(1) { FloatArray(1) }
        Interpreter(input, options).use { it.run(input, output) }
    }
}
