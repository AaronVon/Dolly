package com.pioneer.aaron.dolly.fork;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogData;
import com.pioneer.aaron.dolly.utils.Matrix;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Aaron on 5/1/17.
 */

public class ForkTask extends AsyncTask<Object, Integer, Integer> {
    private static final String TAG = "ForkTask";

    public static final int TYPE_COMPLETED = 1;
    public static final int TYPE_CANCELED = 2;
    public static final int TYPE_FAILED = 3;

    public static final int FORK_TYPE_RANDOM_CALLLOGS = 1;
    public static final int FORK_TYPE_SPECIFIED_CALLLOGS = 2;
    public static final int FORK_TYPE_RANDOM_CONTACT = 3;
    public static final int FORK_TYPE_RANDOM_RCS_CALLLOGS = 4;
    public static final int FORK_TYPE_SPECIFIED_RCS_CALLLOGS = 5;
    public static final int FORK_TYPE_ALL_TYPE_CONTACT = 6;

    public static final int CONTACT_POSSIBLE_NUM_COUNT = 5;

    private static final int FORK_BULK_SIZE = 10;
    private Context mContext;
    private IForkListener mForkListener;
    private boolean mForkCanceled;
    private int mLastProgress;

    public ForkTask(IForkListener forkListener, Context context) {
        this.mForkListener = forkListener;
        this.mContext = context;
        mLastProgress = -1;
        mForkCanceled = false;
    }

    @Override
    protected Integer doInBackground(Object... params) {
        int result = TYPE_FAILED;
        if (params[0] instanceof Integer) {
            int type = (int) params[0];
            Matrix.loadResources(mContext, false);
            switch (type) {
                case FORK_TYPE_RANDOM_CALLLOGS:
                    result = forkRandomCallLogs((int) params[1], false);
                    break;
                case FORK_TYPE_SPECIFIED_CALLLOGS:
                    result = forkSpecifiedCallLog((ForkCallLogData) params[2], false);
                    break;
                case FORK_TYPE_RANDOM_CONTACT:
                    result = forkContacts((int) params[1], false, (Boolean) params[2]);
                    break;
                case FORK_TYPE_ALL_TYPE_CONTACT:
                    result = forkContacts((int) params[1], true, (Boolean) params[2]);
                    break;
                case FORK_TYPE_RANDOM_RCS_CALLLOGS:
                    result = forkRandomCallLogs((int) params[1], true);
                    break;
                case FORK_TYPE_SPECIFIED_RCS_CALLLOGS:
                    result = forkSpecifiedCallLog((ForkCallLogData) params[2], true);
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

    private int forkRandomCallLogs(int quantity, boolean isRCS) {
        if (mContext == null || quantity <= 0) {
            return TYPE_FAILED;
        }
        HashMap<String, Boolean> columnsExists = DataBaseOperator.getInstance(mContext).getColumnsExists();

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        int bulkSize = 0;
        for (int i = 0; i < quantity; ++i) {
            ContentValues values = new ContentValues();
            int call_log_type = Matrix.getRandomType();
            values.put(CallLog.Calls.NEW, call_log_type == CallLog.Calls.MISSED_TYPE ? 1 : 0);
            values.put(CallLog.Calls.NUMBER, Matrix.getRandomPhoneNumWithExistingContact(mContext));
            values.put(CallLog.Calls.TYPE, call_log_type);
            values.put(CallLog.Calls.DATE, System.currentTimeMillis());
            if (isRCS) {
                values.put(ForkCallLogData.IS_PRIMARY, 1);
                values.put(ForkCallLogData.SUBJECT, Matrix.getRandomSubject());
                values.put(ForkCallLogData.POST_CALL_TEXT, Matrix.getRandomPostCallText());
            } else {
                if (columnsExists.get(ForkCallLogData.ENCRYPT_CALL)) {
                    values.put(ForkCallLogData.ENCRYPT_CALL, Matrix.getRandomEncryptCall());
                }
                if (columnsExists.get(ForkCallLogData.FEATURES)) {
                    values.put(ForkCallLogData.FEATURES, Matrix.getRandomFeatures());
                }
                if (columnsExists.get(ForkCallLogData.CALL_TYPE)) {
                    values.put(ForkCallLogData.CALL_TYPE, Matrix.getRandomCallType());
                }
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
            if (bulkSize >= FORK_BULK_SIZE || i >= quantity - 1) {
                try {
                    mContext.getContentResolver().applyBatch(CallLog.AUTHORITY, operations);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
                operations.clear();
                bulkSize = 0;
                int progress = (int) (i * 100 / quantity);
                publishProgress(progress, i + 1);
            }
        }
        return TYPE_COMPLETED;
    }

    private int total = 0;
    private int forkSpecifiedCallLog(ForkCallLogData data, boolean isRCS) {
        if (mContext == null || data == null) {
            return TYPE_FAILED;
        }
        HashMap<String, Boolean> columnsExists = DataBaseOperator.getInstance(mContext).getColumnsExists();

        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, data.getPhoneNum());
        values.put(CallLog.Calls.TYPE, data.getType());
        values.put(CallLog.Calls.NEW, data.getType() == CallLog.Calls.MISSED_TYPE ? 0 : 1);
        values.put(CallLog.Calls.DATE, System.currentTimeMillis());

        if (isRCS) {
            values.put(ForkCallLogData.IS_PRIMARY, 1);
            values.put(ForkCallLogData.SUBJECT, data.getSubject());
            values.put(ForkCallLogData.POST_CALL_TEXT, data.getPostCallText());
        } else {
            if (columnsExists.get(ForkCallLogData.CALL_TYPE)) {
                values.put(ForkCallLogData.CALL_TYPE, data.getCallType());
            }
            if (columnsExists.get(ForkCallLogData.ENCRYPT_CALL)) {
                values.put(ForkCallLogData.ENCRYPT_CALL, data.getEnryptCall());
            }
            if (columnsExists.get(ForkCallLogData.FEATURES)) {
                values.put(ForkCallLogData.FEATURES, data.getFeatures());
            }
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
            if (bulkSize >= FORK_BULK_SIZE || i >= quantity - 1) {
                try {
                    mContext.getContentResolver().applyBatch(CallLog.AUTHORITY, operations);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
                operations.clear();
                bulkSize = 0;
                int progress = (int) (i * 100 / quantity);
                publishProgress(progress, i + 1);
            }
        }
        return TYPE_COMPLETED;
    }

    private int forkContacts(int quantity, boolean allType, boolean avatarIncluded) {
        if (mContext == null || quantity <= 0) {
            return TYPE_FAILED;
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        int bulkSize = 0;
        for (int i = 0; i < quantity; ++i) {
            int rawContactInsertIndex = operations.size();
            String contactName = Matrix.getRandomName();
            operations.add(ContentProviderOperation
                    .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());
            operations.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName)
                    .build());
            operations.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, Matrix.getRandomPhoneNum())
                    .build());
            if (Matrix.getRandonBoolen()) {
                int size = Matrix.getRandomInt(CONTACT_POSSIBLE_NUM_COUNT);
                for (int j = 0; j < size; ++j) {
                    operations.add(ContentProviderOperation
                            .newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, Matrix.getRandomPhoneNum())
                            .build());
                }
            }
            if (allType) {
                /* E-mail */
                operations.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, Matrix.getRandomEmail())
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, Matrix.getRandomEmailType())
                        .build());

                /* Postal Address */
                operations.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, Matrix.getRandomCountry())
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, Matrix.getRandomPostalType())
                        .build());

                /* IM */
                operations.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Im.DATA, contactName)
                        .withValue(ContactsContract.CommonDataKinds.Im.DATA5, Matrix.getRandomIMProtocolType())
                        .build());

                /* Organization */
                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, Matrix.getRandomOrganization())
                        .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, Matrix.getRandomOrganizationType())
                        .build());

                /* Nickname */
                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Nickname.NAME, contactName)
                        .build());

                /* Website */
                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Website.URL, Matrix.getRandomWebsite(contactName))
                        .withValue(ContactsContract.CommonDataKinds.Website.TYPE, Matrix.getRandomWebsiteType())
                        .build());

                /* Relation */
                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Relation.NAME, Matrix.getRandomName())
                        .withValue(ContactsContract.CommonDataKinds.Relation.TYPE, Matrix.getRandomRelationType())
                        .build());

                /* Event */
                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Event.DATA, Matrix.getRandomEventDate())
                        .withValue(ContactsContract.CommonDataKinds.Event.TYPE, Matrix.getRandomEventType())
                        .build());

                /* Note */
                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE, Matrix.getRandomNote())
                        .build());
            }

            /* Avatar */
            if (avatarIncluded) {
                byte[] avatarBytes = Matrix.getRandomAvatar(mContext);
                if (avatarBytes != null) {
                    operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, avatarBytes)
                            .build());
                } else {
                    Log.d(TAG, "Failed to set avatar due to null byte[]");
                }
            }

            ++bulkSize;
            if (mForkCanceled) {
                return TYPE_CANCELED;
            }
            if (bulkSize >= FORK_BULK_SIZE || i >= quantity - 1) {
                try {
                    mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
                operations.clear();
                bulkSize = 0;
                int progress = (int) (i * 100 / quantity);
                publishProgress(progress, i + 1);
            }
        }

        return TYPE_COMPLETED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > mLastProgress) {
            mForkListener.onProgress(progress, values[1]);
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
