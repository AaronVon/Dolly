package com.pioneer.aaron.dolly

import android.Manifest
import android.app.Application
import com.pioneer.aaron.dolly.utils.Matrix
import com.pioneer.aaron.dolly.utils.isPermissionsGranted
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ForkApplication : Application() {
    companion object {
        const val TAG = "ForkApplication"
    }

    override fun onCreate() {
        super.onCreate()
        GlobalScope.launch {
            launch {
                // this is the only chance to preloadContactPhoneNums, for now.
                if (isPermissionsGranted(applicationContext, listOf(Manifest.permission.WRITE_CONTACTS))) {
                    Matrix.preloadContactPhoneNums(applicationContext)
                }
            }
            launch {
                Matrix.loadResources(applicationContext)
            }
        }
    }
}