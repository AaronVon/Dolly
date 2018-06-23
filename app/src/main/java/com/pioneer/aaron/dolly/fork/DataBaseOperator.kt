package com.pioneer.aaron.dolly.fork

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by Aaron on 4/27/17.
 */

class DataBaseOperator private constructor(mContext: Context) {
    private val mSingleExecutorService: ExecutorService = Executors.newSingleThreadExecutor()
    private var mColumnExists: HashMap<String, Boolean>? = null

    private val mCheckColumnsRunnable = Runnable { mColumnExists = checkColumnsExists(*DataBaseOperator.CALLLOG_COLUMNS) }

    val columnsExists: HashMap<String, Boolean>
        get() = if (mColumnExists == null || mColumnExists!!.isEmpty()) {
            checkColumnsExists(*DataBaseOperator.CALLLOG_COLUMNS)
        } else {
            mColumnExists!!
        }

    private var mWeakRef: WeakReference<Context>

    init {
        mSingleExecutorService.execute(mCheckColumnsRunnable)
        mWeakRef = WeakReference(mContext)
    }

    private fun checkColumnsExists(vararg columns: String): HashMap<String, Boolean> {
        mColumnExists = HashMap()
        val hashMap = HashMap<String, Boolean>()
        val cursor: Cursor?
        if (mWeakRef.get() == null) {
            return hashMap
        }
        val contentResolver = mWeakRef.get()!!.contentResolver
        cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, null, null)

        if (cursor != null) {
            for (column in columns) {
                hashMap[column] = cursor.getColumnIndex(column) != -1
            }
            cursor.close()
        }
        mColumnExists = hashMap
        return hashMap
    }

    companion object {
        private val TAG = "DataBaseOperator"

        const val CALLLOG_ENCRYPT = "encrypt_call"
        const val CALLLOG_FEATURE = "features"
        const val CALLLOG_CALL_TYPE = "call_type"
        const val CALLLOG_IS_PRIMARY = "is_primary"
        const val CALLLOG_SUBJECT = "subject"
        const val CALLLOG_POST_CALL_TEXT = "post_call_text"

        val CALLLOG_COLUMNS = arrayOf(CALLLOG_CALL_TYPE, CALLLOG_ENCRYPT, CALLLOG_FEATURE, CALLLOG_IS_PRIMARY, CALLLOG_SUBJECT, CALLLOG_POST_CALL_TEXT)

        @Volatile
        private var INSTANCE: DataBaseOperator? = null

        /**
         * @param context [Application.getApplicationContext] ONLY!!!
         * @return
         */
        fun getInstance(context: Context): DataBaseOperator {
            if (INSTANCE == null) {
                synchronized(DataBaseOperator::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = DataBaseOperator(context)
                    }
                }
            }
            return INSTANCE!!
        }
    }
}
