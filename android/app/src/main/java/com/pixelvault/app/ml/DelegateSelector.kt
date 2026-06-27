package com.pixelvault.app.ml

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DelegateSelector @Inject constructor(
    private val context: Context
) {
    private var selectedDelegate: DelegateType? = null

    enum class DelegateType { NNAPI, GPU, CPU }

    fun select(): DelegateType {
        selectedDelegate?.let { return it }

        val crashFlag = File(context.cacheDir, "nnapi_crash")
        if (!crashFlag.exists() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val delegate = NnApiDelegate.Options().apply {
                    setAllowFp16(true)
                    setUseNnapiCpu(false)
                }
                val nnapi = NnApiDelegate(delegate)
                nnapi.close()
                selectedDelegate = DelegateType.NNAPI
                return DelegateType.NNAPI
            } catch (_: Throwable) {
                crashFlag.createNewFile()
            }
        }

        selectedDelegate = DelegateType.CPU
        return DelegateType.CPU
    }
}
