package com.pioneer.aaron.dolly.fork;

import android.app.Application;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.ContactsContract;

import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogData;
import com.pioneer.aaron.dolly.utils.Matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Aaron on 4/27/17.
 */

public class DataBaseOperator {
    private static final String TAG = "aaron";

    public static final String CALLLOG_ENCRYPT = "encrypt_call";
    public static final String CALLLOG_FEATURE = "features";
    public static final String CALLLOG_CALL_TYPE = "call_type";
    public static final String CALLLOG_IS_PRIMARY = "is_primary";
    public static final String CALLLOG_SUBJECT = "subject";
    public static final String CALLLOG_POST_CALL_TEXT = "post_call_text";

    public final static String[] CALLLOG_COLUMNS = new String[]{
            CALLLOG_CALL_TYPE,
            CALLLOG_ENCRYPT,
            CALLLOG_FEATURE,
            CALLLOG_IS_PRIMARY,
            CALLLOG_SUBJECT,
            CALLLOG_POST_CALL_TEXT
    };

    private static volatile DataBaseOperator INSTANCE;
    private Context mContext;
    private ExecutorService mSingleExecutorService;
    private HashMap<String, Boolean> mColumnExists;

    private DataBaseOperator(Context context) {
        mContext = context;
        mSingleExecutorService = Executors.newSingleThreadExecutor();
        mSingleExecutorService.execute(mCheckColumnsRunnable);
    }

    /**
     * @param context {@link Application#getApplicationContext()} ONLY!!!
     * @return
     */
    public static DataBaseOperator getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DataBaseOperator.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DataBaseOperator(context);
                }
            }
        }
        return INSTANCE;
    }

    private Runnable mCheckColumnsRunnable = new Runnable() {
        @Override
        public void run() {
            mColumnExists = checkColumnsExists(DataBaseOperator.CALLLOG_COLUMNS);
        }
    };

    private HashMap<String, Boolean> checkColumnsExists(String... columns) {
        mColumnExists = new HashMap<>();
        HashMap<String, Boolean> hashMap = new HashMap<>();
        Cursor cursor = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, null, null);

        if (cursor != null) {
            for (String column : columns) {
                hashMap.put(column, cursor.getColumnIndex(column) != -1);
            }
        }
        mColumnExists = hashMap;
        return hashMap;
    }

    public HashMap<String, Boolean> getColumnsExists() {
        if (mColumnExists == null || mColumnExists.isEmpty()) {
            return checkColumnsExists(DataBaseOperator.CALLLOG_COLUMNS);
        } else {
            return mColumnExists;
        }
    }
}
