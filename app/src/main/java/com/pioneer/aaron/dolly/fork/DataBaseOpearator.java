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

import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogData;
import com.pioneer.aaron.dolly.utils.Matrix;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Aaron on 4/27/17.
 */

public class DataBaseOpearator {
    private static final String TAG = "aaron";

    public static final String CALLLOG_ENCRYPT = "encrypt_call";
    public static final String CALLLOG_FEATURE = "features";
    public static final String CALLLOG_CALL_TYPE = "call_type";

    public final static String[] CALLLOG_COLUMNS = new String[]{
            CALLLOG_CALL_TYPE,
            CALLLOG_ENCRYPT,
            CALLLOG_FEATURE
    };

    private static volatile DataBaseOpearator INSTANCE;
    private Context mContext;

    private HashMap<String, Boolean> mColumnExists = new HashMap<>();

    private DataBaseOpearator(Context context) {
        mContext = context;
    }

    /**
     * @param context {@link Application#getApplicationContext()} ONLY!!!
     * @return
     */
    public static DataBaseOpearator getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DataBaseOpearator.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DataBaseOpearator(context);
                }
            }
        }
        return INSTANCE;
    }

    public HashMap<String, Boolean> checkColumnsExists(String... columns) {
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

    public void forkRandomCallLogs(Context context, int quantity) {
        if (context == null || quantity <= 0) {
            return;
        }
        // TODO: 4/29/17 Make this Async
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (int i = 0; i < quantity; ++i) {
            ContentValues values = new ContentValues();
            values.put(CallLog.Calls.NUMBER, Matrix.getRandomPhoneNum());
            values.put(CallLog.Calls.TYPE, Matrix.getRandomType());
            values.put(CallLog.Calls.DATE, System.currentTimeMillis());
            if (mColumnExists.get(ForkCallLogData.CALL_TYPE)) {
                values.put(ForkCallLogData.CALL_TYPE, Matrix.getRandomCallType());
            }
            if (mColumnExists.get(ForkCallLogData.ENCRYPT_CALL)) {
                values.put(ForkCallLogData.ENCRYPT_CALL, Matrix.getRandomEncryptCall());
            }
            if (mColumnExists.get(ForkCallLogData.FEATURES)) {
                values.put(ForkCallLogData.FEATURES, Matrix.getRandomFeatures());
            }
            operations.add(ContentProviderOperation
                    .newInsert(CallLog.Calls.CONTENT_URI)
                    .withValues(values)
                    .withYieldAllowed(true)
                    .build());
        }
        try {
            context.getContentResolver().applyBatch(CallLog.AUTHORITY, operations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public void forkSpecifiedCallLog(Context context, ForkCallLogData data) {
        if (context == null || data == null) {
            return;
        }
        // TODO: 4/29/17 Make this Async
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, data.getPhoneNum());
        values.put(CallLog.Calls.TYPE, data.getType());
        values.put(CallLog.Calls.DATE, System.currentTimeMillis());
        if (mColumnExists.get(ForkCallLogData.CALL_TYPE)) {
            values.put(ForkCallLogData.CALL_TYPE, data.getCallType());
        }
        if (mColumnExists.get(ForkCallLogData.ENCRYPT_CALL)) {
            values.put(ForkCallLogData.ENCRYPT_CALL, data.getEnryptCall());
        }
        if (mColumnExists.get(ForkCallLogData.FEATURES)) {
            values.put(ForkCallLogData.FEATURES, data.getFeatures());
        }

        ContentProviderOperation operation = ContentProviderOperation
                .newInsert(CallLog.Calls.CONTENT_URI)
                .withValues(values)
                .withYieldAllowed(true)
                .build();

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (int i = 0; i < data.getQuantity(); ++i) {
            operations.add(operation);
        }

        try {
            context.getContentResolver().applyBatch(CallLog.AUTHORITY, operations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

}
