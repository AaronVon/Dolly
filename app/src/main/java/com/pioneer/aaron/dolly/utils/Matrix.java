package com.pioneer.aaron.dolly.utils;

import android.content.Context;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
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
     * There are 4 different type of {@link android.provider.ContactsContract.CommonDataKinds.Email}
     * {@link android.provider.ContactsContract.CommonDataKinds.Email#TYPE_CUSTOM} is excluded.
     */
    private static final int EMAIL_TYPE_SPAN = 4;

    private static final String WWW = "www.";
    private static final String EMAIL_AT = "@";
    private static final String DOT_COM = ".com";
    private static final int EMAIL_NAME_LENGTH = 8;
    private static final int EMAIL_DOMAIN_LENGTH = 3;

    private static final int POSTAL_TYPE_SPAN = 3;

    private static final int IM_PROTOCOL_TYPE_SPAN = 10;

    private static final int WEBSITE_TYPE_SPAN = 6;

    private static final int RELATION_TYPE_SPAN = 14;

    private static final String DASH = "-";

    private static final int EVENT_TYPE_SPAN = 3;
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

    private static final String ORGANIZATION_FILE = "fortune_global_500.txt";
    private static ArrayList<String> mOrganizations = null;
    private static int mOrganizationSize;

    private static Locale[] mLocales = null;
    private static int mLocaleSize = 0;

    private static final Object sLock = new Object();

    /**
     * Load resources such as name, and organization, etc.
     *
     * @param context
     * @param hardReset
     */
    public static void loadResources(Context context, boolean hardReset) {
        synchronized (sLock) {
            if (hardReset) {
                loadPredefinedOrganization(context);
                loadPredefinedChineseName(context);
                loadPredefinedEnglishName(context);
                setLocales();
            } else {
                if (mOrganizations == null) {
                    loadPredefinedOrganization(context);
                }
                if (mChineseName == null) {
                    loadPredefinedChineseName(context);
                }
                if (mEnglishName == null) {
                    loadPredefinedEnglishName(context);
                }
                if (mLocales == null) {
                    setLocales();
                }
            }
        }
    }

    private static void loadPredefinedOrganization(Context context) {
        if (mOrganizations != null) {
            mOrganizations.clear();
        } else {
            mOrganizations = new ArrayList<>();
        }
        mOrganizations = readFromTextFile(context, ORGANIZATION_FILE);
        mOrganizationSize = mOrganizations.size();
    }

    private static void loadPredefinedEnglishName(Context context) {
        if (mEnglishName != null) {
            mEnglishName.clear();
        } else {
            mEnglishName = new ArrayList<>();
        }
        mEnglishName = readFromTextFile(context, ENGLISH_NAME_FILE);
        mEnglishNameSize = mEnglishName.size();
    }

    private static void loadPredefinedChineseName(Context context) {
        if (mChineseName != null) {
            mChineseName.clear();
        } else {
            mChineseName = new ArrayList<>();
        }
        mChineseName = readFromTextFile(context, CHINESE_NAME_FILE);
        mChineseNameSize = mChineseName.size();
    }

    private static ArrayList<String> readFromTextFile(Context context, String fileName) {
        if (context == null) {
            Log.e(TAG, "readFromTextFile: Failed. context is NULL");
        }
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                arrayList.add(line);
            }
            inputStream.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return arrayList;
    }

    private static void setLocales() {
        if (mLocales == null) {
            mLocales = Locale.getAvailableLocales();
            mLocaleSize = mLocales.length;
        }
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

    public static String getRandomEmail() {
        StringBuilder email = new StringBuilder();
        long curTime = System.nanoTime();
        String name = String.valueOf(curTime);
        name = name.substring(name.length() - EMAIL_NAME_LENGTH);
        String domain = String.valueOf(curTime);
        domain = domain.substring(domain.length() - EMAIL_DOMAIN_LENGTH);

        return email.append(name)
                .append(EMAIL_AT)
                .append(domain)
                .append(DOT_COM)
                .toString();
    }

    public static int getRandomEmailType() {
        return sRandom.nextInt(EMAIL_TYPE_SPAN) + 1;
    }

    /**
     * @return location based on {@link Locale}.
     */
    public static String getRandomCountry() {
        String location = null;
        do {
            int index = sRandom.nextInt(mLocaleSize);
            location = mLocales[index].getDisplayCountry();
        } while (TextUtils.isEmpty(location));
        return location;
    }

    /**
     * Vary from 1 ~ 3.
     *
     * @return
     */
    public static int getRandomPostalType() {
        return sRandom.nextInt(POSTAL_TYPE_SPAN) + 1;
    }

    /**
     * Vary from -1 ~ 8.
     *
     * @return based on {@link android.provider.ContactsContract.CommonDataKinds.Im#PROTOCOL}
     */
    public static int getRandomIMProtocolType() {
        return sRandom.nextInt(IM_PROTOCOL_TYPE_SPAN) - 1;
    }

    /**
     * Based on Fortune Global 500 of 2017.
     *
     * @return
     */
    public static String getRandomOrganization() {
        return mOrganizations.get(sRandom.nextInt(mOrganizationSize));
    }

    public static int getRandomOrganizationType() {
        return sRandom.nextBoolean() ?
                ContactsContract.CommonDataKinds.Organization.TYPE_WORK : ContactsContract.CommonDataKinds.Organization.TYPE_OTHER;
    }

    public static String getRandomWebsite(String name) {
        return WWW + name + DOT_COM;
    }

    /**
     * See reference {@link android.provider.ContactsContract.CommonDataKinds.Website#TYPE}
     *
     * @return
     */
    public static int getRandomWebsiteType() {
        return sRandom.nextInt(WEBSITE_TYPE_SPAN) + 1;
    }

    /**
     * See reference {@link android.provider.ContactsContract.CommonDataKinds.Relation#TYPE}
     *
     * @return
     */
    public static int getRandomRelationType() {
        return sRandom.nextInt(RELATION_TYPE_SPAN) + 1;
    }

    public static String getRandomEventDate() {
        // currently, get today;
        StringBuilder eventDate = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        eventDate.append(year).append(DASH)
                .append(month).append(DASH)
                .append(day);
        return eventDate.toString();
    }

    public static int getRandomEventType() {
        return sRandom.nextInt(EVENT_TYPE_SPAN) + 1;
    }

    public static String getRandomNote() {
        return "https://github.com/aaronvon";
    }
}
