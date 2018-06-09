package com.pioneer.aaron.dolly.utils

import android.content.Context
import android.os.Vibrator

/**
 * Created by Aaron on 11/9/17.
 */

class ForkVibrator private constructor(context: Context) {
    private val mVibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    fun vibrate(milliseconds: Int) {
        mVibrator.vibrate(milliseconds.toLong())
    }

    companion object {

        private var sForkVibrator: ForkVibrator? = null

        fun getInstance(context: Context): ForkVibrator? {
            if (sForkVibrator == null) {
                synchronized(ForkVibrator::class.java) {
                    sForkVibrator = ForkVibrator(context)
                }
            }
            return sForkVibrator
        }
    }

}
