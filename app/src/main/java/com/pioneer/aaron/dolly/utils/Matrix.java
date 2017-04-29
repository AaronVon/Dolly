package com.pioneer.aaron.dolly.utils;

import android.provider.CallLog;

import java.util.Random;

/**
 * Created by Aaron on 4/29/17.
 */

public class Matrix {

    private static final String TAG = "Matrix";
    private static Random sRandom = new Random();
    /**
     * There are 6 different type of {@link android.provider.CallLog.Calls#TYPE}
     */
    private static final int CALLS_TYPE_SPAN = 7;

    /**
     * <p> 1 for HD;</p>
     * <p> 2 for VoLTE;</p>
     * <p> 3 for WoWiFi;</p>
     */
    private static final int CALLS_CALL_TYPE_SPAN = 4;

    public static String getRandomPhoneNum() {
        int num = Math.abs(sRandom.nextInt());
        return String.valueOf(num);
    }

    /**
     * @return A random {@link android.provider.CallLog.Calls#TYPE}
     */
    public static int getRandomType() {
        int type = Math.abs(sRandom.nextInt(CALLS_TYPE_SPAN));
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE:
                type = CallLog.Calls.INCOMING_TYPE;
                break;
            case CallLog.Calls.OUTGOING_TYPE:
                type = CallLog.Calls.OUTGOING_TYPE;
                break;
            case CallLog.Calls.MISSED_TYPE:
                type = CallLog.Calls.MISSED_TYPE;
                break;
            case CallLog.Calls.REJECTED_TYPE:
                type = CallLog.Calls.REJECTED_TYPE;
                break;
            default:
                type = CallLog.Calls.OUTGOING_TYPE;
        }
        return type;
    }

    /**
     * @return get a random "call_type".
     */
    public static int getRandomCallType() {
        return Math.abs(sRandom.nextInt(CALLS_CALL_TYPE_SPAN));
    }

    public static boolean getRandomEncryptCall() {
        return sRandom.nextBoolean();
    }

    public static int getRandomFeatures() {
        return sRandom.nextBoolean() ? 1 : 0;
    }
}
