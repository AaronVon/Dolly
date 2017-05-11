package com.pioneer.aaron.dolly.utils;

import android.content.Context;
import android.provider.CallLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

    private static final int ENGLISH_NAME_SUFIX = 10;
    private static final int CHINESE_NAME_SUFIX = 4;

    private static final String ENGLISH_NAME_FILE = "EnglishName.txt";
    private static final String CHINESE_NAME_FILE = "ChineseName.txt";
    private static ArrayList<String> mEnglishName;
    private static int mEnglishNameSize;
    private static ArrayList<String> mChineseName;
    private static int mChineseNameSize;

    public static void setNameRes(Context context, boolean hardReset) {
        if (mEnglishName == null || mChineseName == null
                || hardReset) {
            mEnglishName = new ArrayList<>();
            mChineseName = new ArrayList<>();
            setEnglishName(context);
            setChineseName(context);
        }
    }

    private static void setEnglishName(Context context) {
        if (mEnglishName != null) {
            mEnglishName.clear();
        }
        try {
            InputStream inputStream = context.getAssets().open(ENGLISH_NAME_FILE);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                mEnglishName.add(line);
            }
            inputStream.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        mEnglishNameSize = mEnglishName.size();
    }

    private static void setChineseName(Context context) {
        if (mChineseName != null) {
            mChineseName.clear();
        }
        try {
            InputStream inputStream = context.getAssets().open(CHINESE_NAME_FILE);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                mChineseName.add(line);
            }
            inputStream.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        mChineseNameSize = mChineseName.size();
    }


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

    public static String getRandomName() {
        boolean getEnglish = sRandom.nextBoolean();
        StringBuilder name = new StringBuilder();
        if (getEnglish) {
            int index = Math.abs(sRandom.nextInt(mEnglishNameSize));
            name.append(mEnglishName.get(index))
                    .append(Math.abs(sRandom.nextInt(ENGLISH_NAME_SUFIX)));
        } else {
            int len = Math.abs(sRandom.nextInt(CHINESE_NAME_SUFIX)) + 1;
            for (int i = 0; i < len; ++i) {
                name.append(mChineseName.get(Math.abs(sRandom.nextInt(mChineseNameSize))));
            }
        }
        return name.toString();
    }


    /**
     * @return A random RCS Subject.
     */
    public static String getRandomSubject() {
        return getRandomName();
    }

    public static String getRandomPostCallText() {
        return getRandomName();
    }
}
