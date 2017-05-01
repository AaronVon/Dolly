package com.pioneer.aaron.dolly.fork;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.ContactsContract;

import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogData;
import com.pioneer.aaron.dolly.utils.Matrix;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Aaron on 5/1/17.
 */

public class ForkTask extends AsyncTask<Object, Integer, Integer> {

    public static final int TYPE_COMPLETED = 1;
    public static final int TYPE_CANCELED = 2;
    public static final int TYPE_FAILED = 3;

    public static final int FORK_TYPE_RANDOM_CALLLOGS = 1;
    public static final int FORK_TYPE_SPECIFIED_CALLLOGS = 2;
    public static final int FORK_TYPE_RANDOM_CONTACT = 3;

    private static final int FORK_BULK_SIZE = 10;
    private Context mContext;
    private IForkListener mForkListener;
    private boolean mForkCanceled;
    private int mLastProgress;

    public ForkTask(IForkListener forkListener, Context context) {
        this.mForkListener = forkListener;
        this.mContext = context;
        mLastProgress = -1 ;
        mForkCanceled = false;
    }

    @Override
    protected Integer doInBackground(Object... params) {
        int result = TYPE_FAILED;
        if (params[0] instanceof Integer) {
            int type = (int) params[0];
            switch (type) {
                case FORK_TYPE_RANDOM_CALLLOGS:
                    result = forkRandomCallLogs((int) params[1]);
                    break;
                case FORK_TYPE_SPECIFIED_CALLLOGS:
                    result = forkSpecifiedCallLog((ForkCallLogData) params[2]);
                    break;
                case FORK_TYPE_RANDOM_CONTACT:
                    result = forkContacts((int) params[1]);
                    break;
                default:
                    result = TYPE_FAILED;
                    break;
            }
        } else {
            result = TYPE_FAILED;
        }
        return result;
    }

    private int forkRandomCallLogs(int quantity) {
        if (mContext == null || quantity <= 0) {
            return TYPE_FAILED;
        }
        HashMap<String, Boolean> columnsExists = DataBaseOpearator.getInstance(mContext).getColumnsExists();

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        int bulkSize = 0;
        for (int i = 0; i < quantity; ++i) {
            ContentValues values = new ContentValues();
            values.put(CallLog.Calls.NUMBER, Matrix.getRandomPhoneNum());
            values.put(CallLog.Calls.TYPE, Matrix.getRandomType());
            values.put(CallLog.Calls.DATE, System.currentTimeMillis());
            if (columnsExists.get(ForkCallLogData.CALL_TYPE)) {
                values.put(ForkCallLogData.CALL_TYPE, Matrix.getRandomCallType());
            }
            if (columnsExists.get(ForkCallLogData.ENCRYPT_CALL)) {
                values.put(ForkCallLogData.ENCRYPT_CALL, Matrix.getRandomEncryptCall());
            }
            if (columnsExists.get(ForkCallLogData.FEATURES)) {
                values.put(ForkCallLogData.FEATURES, Matrix.getRandomFeatures());
            }
            operations.add(ContentProviderOperation
                    .newInsert(CallLog.Calls.CONTENT_URI)
                    .withValues(values)
                    .withYieldAllowed(true)
                    .build());
            ++bulkSize;
            if (mForkCanceled) {
                return TYPE_CANCELED;
            }
            if (bulkSize >= FORK_BULK_SIZE || i >= quantity) {
                try {
                    mContext.getContentResolver().applyBatch(CallLog.AUTHORITY, operations);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
                bulkSize = 0;
                int progress = (int) (i * 100 / quantity);
                publishProgress(progress);
            }
        }

        return TYPE_COMPLETED;
    }

    private int forkSpecifiedCallLog(ForkCallLogData data) {
        if (mContext == null || data == null) {
            return TYPE_FAILED;
        }
        HashMap<String, Boolean> columnsExists = DataBaseOpearator.getInstance(mContext).getColumnsExists();

        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, data.getPhoneNum());
        values.put(CallLog.Calls.TYPE, data.getType());
        values.put(CallLog.Calls.DATE, System.currentTimeMillis());
        if (columnsExists.get(ForkCallLogData.CALL_TYPE)) {
            values.put(ForkCallLogData.CALL_TYPE, data.getCallType());
        }
        if (columnsExists.get(ForkCallLogData.ENCRYPT_CALL)) {
            values.put(ForkCallLogData.ENCRYPT_CALL, data.getEnryptCall());
        }
        if (columnsExists.get(ForkCallLogData.FEATURES)) {
            values.put(ForkCallLogData.FEATURES, data.getFeatures());
        }

        ContentProviderOperation operation = ContentProviderOperation
                .newInsert(CallLog.Calls.CONTENT_URI)
                .withValues(values)
                .withYieldAllowed(true)
                .build();

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        int bulkSize = 0;
        int quantity = data.getQuantity();
        for (int i = 0; i < quantity; ++i) {
            operations.add(operation);
            ++bulkSize;
            if (mForkCanceled) {
                return TYPE_CANCELED;
            }
            if (bulkSize >= FORK_BULK_SIZE || i >= quantity) {
                try {
                    mContext.getContentResolver().applyBatch(CallLog.AUTHORITY, operations);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
                bulkSize = 0;
                int progress = (int) (i * 100 / quantity);
                publishProgress(progress);
            }
        }
        return TYPE_COMPLETED;
    }

    private int forkContacts(int quantity) {
        if (mContext == null || quantity <= 0) {
            return TYPE_FAILED;
        }
        Matrix.setNameRes(mContext, false);

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        int bulkSize = 0;
        for (int i = 0; i < quantity; ++i) {
            int rawContacInsertIndex = operations.size();
            operations.add(ContentProviderOperation
                    .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());
            operations.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContacInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, Matrix.getRandomName())
                    .build());
            operations.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContacInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, Matrix.getRandomPhoneNum())
                    .build());
            ++bulkSize;
            if (mForkCanceled) {
                return TYPE_CANCELED;
            }
            if (bulkSize >= FORK_BULK_SIZE || i >= quantity) {
                try {
                    mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
                bulkSize = 0;
                int progress = (int) (i * 100 / quantity);
                publishProgress(progress);
            }
        }

        return TYPE_COMPLETED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > mLastProgress) {
            mForkListener.onProgress(progress);
            mLastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer status) {
        switch (status) {
            case TYPE_COMPLETED:
                mForkListener.onCompleted();
                break;
            case TYPE_CANCELED:
                mForkListener.onCanceled();
                break;
            default:
                mForkListener.onFailed();
                break;
        }
    }

    public void cancelFork() {
        mForkCanceled = true;
    }
}
