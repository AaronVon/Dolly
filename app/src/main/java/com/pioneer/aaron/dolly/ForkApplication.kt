package com.pioneer.aaron.dolly

import android.app.Application
import android.util.Log
import com.pioneer.aaron.dolly.utils.Matrix
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ForkApplication : Application() {
    companion object {
        const val TAG = "ForkApplication"
    }

    override fun onCreate() {
        super.onCreate()
        GlobalScope.launch {
            launch {
                Matrix.preloadContactPhoneNums(applicationContext)
            }
            launch {
                Matrix.loadResources(applicationContext)
            }
        }
    }
}