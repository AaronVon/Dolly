package com.pioneer.aaron.dolly.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.provider.BaseColumns
import android.provider.CallLog
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import com.pioneer.aaron.dolly.R
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Aaron on 4/29/17.
 */

object Matrix {

    private const val TAG = "Matrix"
    private val sRandom = Random()
    /**
     * There are 6 different type of [android.provider.CallLog.Calls.TYPE]
     */
    private const val CALLS_TYPE_SPAN = 7

    /**
     * There are 4 different type of [android.provider.ContactsContract.CommonDataKinds.Email]
     * [android.provider.ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM] is excluded.
     */
    private const val EMAIL_TYPE_SPAN = 4

    private const val WWW = "www."
    private const val EMAIL_AT = "@"
    private const val DOT_COM = ".com"
    private const val EMAIL_NAME_LENGTH = 8
    private const val EMAIL_DOMAIN_LENGTH = 3

    private const val POSTAL_TYPE_SPAN = 3

    private const val IM_PROTOCOL_TYPE_SPAN = 10

    private const val WEBSITE_TYPE_SPAN = 6

    private const val RELATION_TYPE_SPAN = 14

    private const val DASH = "-"

    private const val EVENT_TYPE_SPAN = 3
    /**
     *
     *  1 for HD;
     *
     *  2 for VoLTE;
     *
     *  3 for WoWiFi;
     */
    private const val CALLS_CALL_TYPE_SPAN = 4

    private const val ENGLISH_NAME_SUFIX = 10
    private const val CHINESE_NAME_SUFIX = 4

    private const val ENGLISH_NAME_FILE = "EnglishName.txt"
    private const val CHINESE_NAME_FILE = "ChineseName.txt"
    private var mEnglishName = ArrayList<String>()
    private var mEnglishNameSize: Int = 0
    private var mChineseName = ArrayList<String>()
    private var mChineseNameSize: Int = 0

    private const val ORGANIZATION_FILE = "fortune_global_500.txt"
    private var mOrganizations = ArrayList<String>()
    private var mOrganizationSize: Int = 0

    private var mLocales = ArrayList<Locale>()
    private var mLocaleSize = 0

    private const val PATH_AVATARS = "avatars"
    private var mAvatarFileNames = ArrayList<String>()

    private val sLock = Any()
    private val sContactNumberLock = Any()

    private var mNumbers = ArrayList<String>()
    private var mNumberSize = 0

    val randomPhoneNum: String
        get() {
            val num = Math.abs(sRandom.nextInt())
            return num.toString()
        }

    /**
     * @return A random [android.provider.CallLog.Calls.TYPE]
     */
    val randomType: Int
        get() {
            var type = Math.abs(sRandom.nextInt(CALLS_TYPE_SPAN))
            type = when (type) {
                CallLog.Calls.INCOMING_TYPE -> CallLog.Calls.INCOMING_TYPE
                CallLog.Calls.OUTGOING_TYPE -> CallLog.Calls.OUTGOING_TYPE
                CallLog.Calls.MISSED_TYPE -> CallLog.Calls.MISSED_TYPE
                CallLog.Calls.REJECTED_TYPE -> CallLog.Calls.REJECTED_TYPE
                else -> CallLog.Calls.OUTGOING_TYPE
            }
            return type
        }

    /**
     * @return get a random "call_type".
     */
    val randomCallType: Int
        get() = Math.abs(sRandom.nextInt(CALLS_CALL_TYPE_SPAN))

    val randomEncryptCall: Boolean
        get() = sRandom.nextBoolean()

    val randomFeatures: Int
        get() = if (sRandom.nextBoolean()) 1 else 0

    val randomName: String
        get() {
            val getEnglish = sRandom.nextBoolean()
            val name = StringBuilder()
            if (getEnglish) {
                val index = Math.abs(sRandom.nextInt(mEnglishNameSize))
                name.append(mEnglishName[index])
                        .append(Math.abs(sRandom.nextInt(ENGLISH_NAME_SUFIX)))
            } else {
                val len = Math.abs(sRandom.nextInt(CHINESE_NAME_SUFIX)) + 1
                for (i in 0 until len) {
                    name.append(mChineseName[Math.abs(sRandom.nextInt(mChineseNameSize))])
                }
            }
            return name.toString()
        }


    /**
     * @return A random RCS Subject.
     */
    val randomSubject: String
        get() = randomName

    val randomPostCallText: String
        get() = randomName

    val randomEmail: String
        get() {
            val email = StringBuilder()
            val curTime = System.nanoTime()
            var name = curTime.toString()
            name = name.substring(name.length - EMAIL_NAME_LENGTH)
            var domain = curTime.toString()
            domain = domain.substring(domain.length - EMAIL_DOMAIN_LENGTH)

            return email.append(name)
                    .append(EMAIL_AT)
                    .append(domain)
                    .append(DOT_COM)
                    .toString()
        }

    val randomEmailType: Int
        get() = sRandom.nextInt(EMAIL_TYPE_SPAN) + 1

    /**
     * @return location based on [Locale].
     */
    val randomCountry: String
        get() {
            var location: String
            do {
                val index = sRandom.nextInt(mLocaleSize)
                location = mLocales[index].displayCountry
            } while (TextUtils.isEmpty(location))
            return location
        }

    /**
     * Vary from 1 ~ 3.
     *
     * @return
     */
    val randomPostalType: Int
        get() = sRandom.nextInt(POSTAL_TYPE_SPAN) + 1

    /**
     * Vary from -1 ~ 8.
     *
     * @return based on [android.provider.ContactsContract.CommonDataKinds.Im.PROTOCOL]
     */
    val randomIMProtocolType: Int
        get() = sRandom.nextInt(IM_PROTOCOL_TYPE_SPAN) - 1

    /**
     * Based on Fortune Global 500 of 2017.
     *
     * @return
     */
    val randomOrganization: String
        get() = mOrganizations[sRandom.nextInt(mOrganizationSize)]

    val randomOrganizationType: Int
        get() = if (sRandom.nextBoolean())
            ContactsContract.CommonDataKinds.Organization.TYPE_WORK
        else
            ContactsContract.CommonDataKinds.Organization.TYPE_OTHER

    /**
     * See reference [android.provider.ContactsContract.CommonDataKinds.Website.TYPE]
     *
     * @return
     */
    val randomWebsiteType: Int
        get() = sRandom.nextInt(WEBSITE_TYPE_SPAN) + 1

    /**
     * See reference [android.provider.ContactsContract.CommonDataKinds.Relation.TYPE]
     *
     * @return
     */
    val randomRelationType: Int
        get() = sRandom.nextInt(RELATION_TYPE_SPAN) + 1

    // currently, get today;
    val randomEventDate: String
        get() {
            val eventDate = StringBuilder()
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            eventDate.append(year).append(DASH)
                    .append(month).append(DASH)
                    .append(day)
            return eventDate.toString()
        }

    val randomEventType: Int
        get() = sRandom.nextInt(EVENT_TYPE_SPAN) + 1

    val randomNote: String
        get() = "https://github.com/aaronvon"

    val randonBoolen: Boolean
        get() = sRandom.nextBoolean()

    val randomSubId: Int
        get() = getRandomInt(2)

    /**
     * Load resources such as name, and organization, etc.
     *
     * @param context
     * @param hardReset
     */
    fun loadResources(context: Context, hardReset: Boolean) {
        synchronized(sLock) {
            if (hardReset) {
                loadPredefinedOrganization(context)
                loadPredefinedChineseName(context)
                loadPredefinedEnglishName(context)
                setLocales()
            } else {
                mOrganizations.isEmpty().let { loadPredefinedOrganization(context) }
                mChineseName.isEmpty().let { loadPredefinedChineseName(context) }
                mEnglishName.isEmpty().let { loadPredefinedEnglishName(context) }
                mLocales.isEmpty().let { setLocales() }
            }
            loadAvatarsFileNames(context)
        }
    }

    private fun loadPredefinedOrganization(context: Context) {
        mOrganizations.clear()
        mOrganizations = readFromTextFile(context, ORGANIZATION_FILE)
        mOrganizationSize = mOrganizations.size
    }

    private fun loadPredefinedEnglishName(context: Context) {
        mEnglishName = readFromTextFile(context, ENGLISH_NAME_FILE)
        mEnglishNameSize = mEnglishName.size
    }

    private fun loadPredefinedChineseName(context: Context) {
        mChineseName = readFromTextFile(context, CHINESE_NAME_FILE)
        mChineseNameSize = mChineseName.size
    }

    private fun readFromTextFile(context: Context, fileName: String): ArrayList<String> {
        val arrayList = ArrayList<String>()
        try {
            val inputStream = context.assets.open(fileName)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val lineList = mutableListOf<String>()
            inputStream.bufferedReader().useLines { ln -> ln.forEach { lineList.add(it) } }
            lineList.forEach { arrayList.add(it) }
            inputStream.close()
            bufferedReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {

        }
        return arrayList
    }

    fun preloadContactPhoneNums(context: Context) {
        synchronized(sContactNumberLock) {
            mNumbers.isEmpty().let {
                val people = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER), null, null, BaseColumns._ID + " LIMIT 500"
                )
                people?.let {
                    val phoneNumColumneIndex = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    people.moveToFirst()
                    while (people.moveToNext()) {
                        mNumbers.add(people.getString(phoneNumColumneIndex))
                    }
                    mNumberSize = mNumbers.size
                    people.close()
                }
            }
        }
    }

    private fun setLocales() {
        val locales = Locale.getAvailableLocales()
        for (locale in locales) {
            mLocales.add(locale)
        }
        mLocaleSize = mLocales.size
    }

    fun getRandomPhoneNumWithExistingContact(context: Context): String {
        mNumbers.isEmpty().let { preloadContactPhoneNums(context) }
        return if (sRandom.nextBoolean()) {
            randomPhoneNum
        } else {
            if (mNumberSize <= 0) {
                randomPhoneNum
            } else {
                mNumbers[sRandom.nextInt(mNumberSize)]
            }
        }
    }

    fun getRandomWebsite(name: String): String {
        return WWW + name + DOT_COM
    }

    fun getRandomInt(range: Int): Int {
        return if (range >= 0) {
            sRandom.nextInt(range)
        } else 1
    }

    private fun loadAvatarsFileNames(context: Context) {
        try {
            val avatars = context.assets.list(PATH_AVATARS)
            for (avatar in avatars) {
                mAvatarFileNames.add(avatar)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun getRandomAvatar(context: Context): ByteArray? {
        var avatarBytes: ByteArray? = null
        var avatar: Bitmap? = null
        var avatarFileName = ""
        if (mAvatarFileNames.isEmpty()) {
            Log.i(TAG, "getRandomAvatar: failed to get avatar assets. Using default avatar")
            avatar = BitmapFactory.decodeResource(context.resources, R.mipmap.fork_icon)
        } else {
            avatarFileName = mAvatarFileNames[sRandom.nextInt(mAvatarFileNames.size)]
            try {
                val avatarInputStream = context.assets.open("$PATH_AVATARS/$avatarFileName")
                avatar = BitmapFactory.decodeStream(avatarInputStream)

                avatarInputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        if (avatar == null) {
            Log.e(TAG, "getRandomAvatar: avatar bitmap is null")
            return null
        }
        val outputStream = ByteArrayOutputStream()
        avatar.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        avatarBytes = outputStream.toByteArray()

        avatar.recycle()
        try {
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return avatarBytes
    }

    class LoadResAsyncTask(context: Context) : AsyncTask<Int, Void, Void>() {
        private val weakReference: WeakReference<Context> = WeakReference(context)

        interface RES_TYPE_LOAD {
            companion object {
                const val CONTACT_NUMBRES = 0
                const val ASSET_RES = 1
            }
        }

        override fun doInBackground(vararg p0: Int?): Void? {
            weakReference.get()?.let {
                when (p0[0]) {
                    RES_TYPE_LOAD.CONTACT_NUMBRES -> Matrix.preloadContactPhoneNums(it)
                    RES_TYPE_LOAD.ASSET_RES -> Matrix.loadResources(it, true)
                    else -> {
                    }
                }
            } ?: Log.e(TAG, "LoadResAsyncTask: Failed to load res due to context is NULL")
            return null
        }
    }
}
