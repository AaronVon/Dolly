package com.pioneer.aaron.dolly.utils;

import android.content.Context;
import android.os.Vibrator;

/**
 * Created by Aaron on 11/9/17.
 */

public class ForkVibrator {

    private static ForkVibrator sForkVibrator;
    private Vibrator mVibrator;

    private ForkVibrator(Context context) {
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public static ForkVibrator getInstance(Context context) {
        if (sForkVibrator == null) {
            synchronized (ForkVibrator.class) {
                sForkVibrator = new ForkVibrator(context);
            }
        }
        return sForkVibrator;
    }

    public void vibrate(int milliseconds) {
        mVibrator.vibrate(milliseconds);
    }

}
