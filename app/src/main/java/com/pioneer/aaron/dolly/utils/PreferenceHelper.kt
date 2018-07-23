package com.pioneer.aaron.dolly.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson

class PreferenceHelper private constructor() {

    lateinit var mForkCallLogsData: ForkCalllogs
    lateinit var mForkContactsData: ForkContacts

    companion object {

        @Volatile
        private var instance: PreferenceHelper? = null

        fun getInstance(context: Context): PreferenceHelper =
                instance ?: synchronized(this) {
                    instance ?: buildPreference(context).also { instance = it }
                }

        private fun buildPreference(context: Context): PreferenceHelper {
            val preferenecUtil = PreferenceHelper()
            preferenecUtil.init(context)
            return preferenecUtil
        }
    }


    private fun init(context: Context) {
        mForkCallLogsData = getSpValue(context, DollySp.DOLLY_SP_CALLLOGS) as ForkCalllogs
        mForkContactsData = getSpValue(context, DollySp.DOLLY_SP_CONTACTS) as ForkContacts
    }

    fun updateForkPreference(context: Context, data: Any) {
        when (data) {
            is ForkCalllogs -> {
                if (!data.equals(mForkCallLogsData)) {
                    mForkCallLogsData = data
                    updateSpValue(context, DollySp.DOLLY_SP_CALLLOGS, data)
                }
            }
            is ForkContacts -> {
                if (!data.equals(mForkContactsData)) {
                    mForkContactsData = data
                    updateSpValue(context, DollySp.DOLLY_SP_CONTACTS, data)
                }
            }
            else -> {
                // do nothing...
            }
        }
    }

    private fun getSpValue(context: Context, key: String): Any {
        val sharedPreferences = context.getSharedPreferences(DollySp.DOLLY_SP, MODE_PRIVATE)
        val jsonStr = sharedPreferences.getString(key, "")
        val data: Any
        if (jsonStr == "") {
            data = when (key) {
                DollySp.DOLLY_SP_CALLLOGS -> {
                    mForkCallLogsData = ForkCalllogs()
                    mForkCallLogsData
                }
                DollySp.DOLLY_SP_CONTACTS -> {
                    mForkContactsData = ForkContacts()
                    mForkContactsData
                }
                else -> {
                    ForkCalllogs()
                }
            }
        } else {
            data = Gson().fromJson(jsonStr, when (key) {
                DollySp.DOLLY_SP_CALLLOGS -> ForkCalllogs::class.java
                DollySp.DOLLY_SP_CONTACTS -> ForkContacts::class.java
                else -> {
                    ForkCalllogs::class.java
                }
            })
        }
        return data
    }

    private fun updateSpValue(context: Context, key: String, data: Any) {
        val dataGson = Gson().toJson(data)
        val sharedPreferences = context.getSharedPreferences(DollySp.DOLLY_SP, MODE_PRIVATE)
        sharedPreferences.edit().putString(key, dataGson).apply()
    }
}

data class ForkCalllogs(var quantity: Int = ForkConstants.CALLLOG_DEFAULT_QUANTITY,
                        var allRandom: Boolean = true)

data class ForkContacts(var quantity: Int = ForkConstants.CONTACT_DEFAULT_QUANTITY,
                        var allTypes: Boolean = true,
                        var genAvatar: Boolean = false)

object DollySp {
    const val DOLLY_SP: String = "DollySp"
    const val DOLLY_SP_CALLLOGS: String = "DollySpCallLogs"
    const val DOLLY_SP_CONTACTS: String = "DollySpContacts"
}