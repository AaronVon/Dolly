package com.pioneer.aaron.dolly.fork

import android.content.*
import android.net.Uri
import android.os.AsyncTask
import android.os.RemoteException
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.VoicemailContract
import android.util.Log
import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogData
import com.pioneer.aaron.dolly.utils.Matrix
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Aaron on 5/1/17.
 */

class ForkTask(private val mForkListener: IForkListener, context: Context) : AsyncTask<Any, Int, Int>() {
    private val mWeakReference: WeakReference<Context> = WeakReference(context)
    private val mBaseUri: Uri
    private val mContentResolver: ContentResolver
    private var mForkCanceled: Boolean = false
    private var mLastProgress: Int = 0

    init {
        mLastProgress = -1
        mForkCanceled = false
        mBaseUri = VoicemailContract.Voicemails.buildSourceUri(context.packageName)
        mContentResolver = context.contentResolver
    }

    override fun doInBackground(vararg params: Any): Int? {
        val context = mWeakReference.get()
        var result = TYPE_FAILED
        if (context == null) {
            Log.i(TAG, "ForkTask.doInBackground failed due to context is NULL")
            return result
        }
        if (params[0] is Int) {
            val type = params[0] as Int
            Matrix.loadResources(context, false)
            result = when (type) {
                FORK_TYPE_RANDOM_CALLLOGS -> forkRandomCallLogs(context, params[1] as Int, false)
                FORK_TYPE_SPECIFIED_CALLLOGS -> forkSpecifiedCallLog(context, params[2] as ForkCallLogData, false)
                FORK_TYPE_RANDOM_CONTACT -> forkContacts(context, params[1] as Int, false, params[2] as Boolean)
                FORK_TYPE_ALL_TYPE_CONTACT -> forkContacts(context, params[1] as Int, true, params[2] as Boolean)
                FORK_TYPE_RANDOM_RCS_CALLLOGS -> forkRandomCallLogs(context, params[1] as Int, true)
                FORK_TYPE_SPECIFIED_RCS_CALLLOGS -> forkSpecifiedCallLog(context, params[2] as ForkCallLogData, true)
                FORK_TYPE_VVM -> forkVvmCallLog(params[1] as ForkCallLogData, context)
                else -> TYPE_FAILED
            }
        } else {
            result = TYPE_FAILED
        }
        return result
    }

    private fun forkVvmCallLog(callLogData: ForkCallLogData, context: Context): Int {
        val phoneNumber = callLogData.phoneNum

        val contentValues = ContentValues()
        contentValues.put(VoicemailContract.Voicemails.NUMBER, phoneNumber)
        contentValues.put(CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME, context.packageName)
        contentValues.put(CallLog.Calls.PHONE_ACCOUNT_ID, callLogData.subId)
        contentValues.put(VoicemailContract.Voicemails.DURATION, 21)
        contentValues.put(VoicemailContract.Voicemails.DATE, System.currentTimeMillis())
        contentValues.put(VoicemailContract.Voicemails.IS_READ, 0)

        val newVoicemailUri = mContentResolver.insert(mBaseUri, contentValues)

        if (newVoicemailUri == null) {
            Log.d(TAG, "Fork VVM failed.")
            return TYPE_FAILED
        }

        setVoicemailContent(newVoicemailUri, context)

        return TYPE_COMPLETED
    }

    private fun setVoicemailContent(voicemailUri: Uri, context: Context) {
        try {
            mContentResolver.openOutputStream(voicemailUri)!!.use { outputStream ->
                val inputStream = context.resources.assets.open("voicemail_demo.m4a")
                copyStreamData(inputStream, outputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val contentValues = ContentValues()
        contentValues.put(VoicemailContract.Voicemails.MIME_TYPE, "audio/amr")
        contentValues.put(VoicemailContract.Voicemails.HAS_CONTENT, true)
        val updateCount = mContentResolver.update(voicemailUri, contentValues, null, null)
        if (updateCount != 1) {
            Log.e(TAG, "Voicemail should only be updated 1 row, but was $updateCount")
        }
    }

    @Throws(IOException::class)
    private fun copyStreamData(inputStream: InputStream, outputStream: OutputStream) {
        val data = ByteArray(8 * 1024)
        var numBytes = inputStream.read(data)
        while (numBytes > 0) {
            outputStream.write(data, 0, numBytes)
            numBytes = inputStream.read(data)
        }
    }

    private fun forkRandomCallLogs(context: Context, quantity: Int, isRCS: Boolean): Int {
        if (quantity <= 0) {
            return TYPE_FAILED
        }
        val columnsExists = DataBaseOperator.getInstance(context)!!.columnsExists

        val operations = ArrayList<ContentProviderOperation>()
        var bulkSize = 0
        for (i in 0 until quantity) {
            val values = ContentValues()
            val call_log_type = Matrix.randomType
            values.put(CallLog.Calls.NEW, if (call_log_type == CallLog.Calls.MISSED_TYPE) 1 else 0)
            values.put(CallLog.Calls.NUMBER, Matrix.getRandomPhoneNumWithExistingContact(context))
            values.put(CallLog.Calls.TYPE, call_log_type)
            values.put(CallLog.Calls.DATE, System.currentTimeMillis())
            if (isRCS) {
                values.put(ForkCallLogData.IS_PRIMARY, 1)
                values.put(ForkCallLogData.SUBJECT, Matrix.randomSubject)
                values.put(ForkCallLogData.POST_CALL_TEXT, Matrix.randomPostCallText)
            } else {
                if (columnsExists[ForkCallLogData.ENCRYPT_CALL]!!) {
                    values.put(ForkCallLogData.ENCRYPT_CALL, Matrix.randomEncryptCall)
                }
                if (columnsExists[ForkCallLogData.FEATURES]!!) {
                    values.put(ForkCallLogData.FEATURES, Matrix.randomFeatures)
                }
                if (columnsExists[ForkCallLogData.CALL_TYPE]!!) {
                    values.put(ForkCallLogData.CALL_TYPE, Matrix.randomCallType)
                }
                values.put(CallLog.Calls.PHONE_ACCOUNT_ID, Matrix.randomSubId)
            }
            operations.add(ContentProviderOperation
                    .newInsert(CallLog.Calls.CONTENT_URI)
                    .withValues(values)
                    .withYieldAllowed(true)
                    .build())
            ++bulkSize
            if (mForkCanceled) {
                return TYPE_CANCELED
            }
            if (bulkSize >= FORK_BULK_SIZE || i >= quantity - 1) {
                try {
                    context.contentResolver.applyBatch(CallLog.AUTHORITY, operations)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                } catch (e: OperationApplicationException) {
                    e.printStackTrace()
                }

                operations.clear()
                bulkSize = 0
                val progress = i * 100 / quantity
                publishProgress(progress, i + 1)
            }
        }
        return TYPE_COMPLETED
    }

    private fun forkSpecifiedCallLog(context: Context?, data: ForkCallLogData?, isRCS: Boolean): Int {
        if (context == null || data == null) {
            return TYPE_FAILED
        }
        val columnsExists = DataBaseOperator.getInstance(context)!!.columnsExists

        val values = ContentValues()
        values.put(CallLog.Calls.NUMBER, data.phoneNum)
        values.put(CallLog.Calls.TYPE, data.type)
        values.put(CallLog.Calls.NEW, if (data.type == CallLog.Calls.MISSED_TYPE) 0 else 1)
        values.put(CallLog.Calls.DATE, System.currentTimeMillis())
        values.put(CallLog.Calls.PHONE_ACCOUNT_ID, data.subId)

        if (isRCS) {
            values.put(ForkCallLogData.IS_PRIMARY, 1)
            values.put(ForkCallLogData.SUBJECT, data.subject)
            values.put(ForkCallLogData.POST_CALL_TEXT, data.postCallText)
        } else {
            if (columnsExists[ForkCallLogData.CALL_TYPE]!!) {
                values.put(ForkCallLogData.CALL_TYPE, data.callType)
            }
            if (columnsExists[ForkCallLogData.ENCRYPT_CALL]!!) {
                values.put(ForkCallLogData.ENCRYPT_CALL, data.enryptCall)
            }
            if (columnsExists[ForkCallLogData.FEATURES]!!) {
                values.put(ForkCallLogData.FEATURES, data.features)
            }
        }

        val operation = ContentProviderOperation
                .newInsert(CallLog.Calls.CONTENT_URI)
                .withValues(values)
                .withYieldAllowed(true)
                .build()

        val operations = ArrayList<ContentProviderOperation>()
        var bulkSize = 0
        val quantity = data.quantity
        for (i in 0 until quantity) {
            operations.add(operation)
            ++bulkSize
            if (mForkCanceled) {
                return TYPE_CANCELED
            }
            if (bulkSize >= FORK_BULK_SIZE || i >= quantity - 1) {
                try {
                    context.contentResolver.applyBatch(CallLog.AUTHORITY, operations)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                } catch (e: OperationApplicationException) {
                    e.printStackTrace()
                }

                operations.clear()
                bulkSize = 0
                val progress = i * 100 / quantity
                publishProgress(progress, i + 1)
            }
        }
        return TYPE_COMPLETED
    }

    private fun forkContacts(context: Context?, quantity: Int, allType: Boolean, avatarIncluded: Boolean): Int {
        if (context == null || quantity <= 0) {
            return TYPE_FAILED
        }

        val operations = ArrayList<ContentProviderOperation>()
        var bulkSize = 0
        for (i in 0 until quantity) {
            val rawContactInsertIndex = operations.size
            val contactName = Matrix.randomName
            operations.add(ContentProviderOperation
                    .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build())
            operations.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName)
                    .build())
            operations.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, Matrix.randomPhoneNum)
                    .build())
            if (Matrix.randonBoolen) {
                val size = Matrix.getRandomInt(CONTACT_POSSIBLE_NUM_COUNT)
                for (j in 0 until size) {
                    operations.add(ContentProviderOperation
                            .newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, Matrix.randomPhoneNum)
                            .build())
                }
            }
            if (allType) {
                /* E-mail */
                operations.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, Matrix.randomEmail)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, Matrix.randomEmailType)
                        .build())

                /* Postal Address */
                operations.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, Matrix.randomCountry)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, Matrix.randomPostalType)
                        .build())

                /* IM */
                operations.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Im.DATA, contactName)
                        .withValue(ContactsContract.CommonDataKinds.Im.DATA5, Matrix.randomIMProtocolType)
                        .build())

                /* Organization */
                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, Matrix.randomOrganization)
                        .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, Matrix.randomOrganizationType)
                        .build())

                /* Nickname */
                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Nickname.NAME, contactName)
                        .build())

                /* Website */
                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Website.URL, Matrix.getRandomWebsite(contactName))
                        .withValue(ContactsContract.CommonDataKinds.Website.TYPE, Matrix.randomWebsiteType)
                        .build())

                /* Relation */
                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Relation.NAME, Matrix.randomName)
                        .withValue(ContactsContract.CommonDataKinds.Relation.TYPE, Matrix.randomRelationType)
                        .build())

                /* Event */
                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Event.DATA, Matrix.randomEventDate)
                        .withValue(ContactsContract.CommonDataKinds.Event.TYPE, Matrix.randomEventType)
                        .build())

                /* Note */
                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE, Matrix.randomNote)
                        .build())
            }

            /* Avatar */
            if (avatarIncluded) {
                val avatarBytes = Matrix.getRandomAvatar(context)
                if (avatarBytes != null) {
                    operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, avatarBytes)
                            .build())
                } else {
                    Log.d(TAG, "Failed to set avatar due to null byte[]")
                }
            }

            ++bulkSize
            if (mForkCanceled) {
                return TYPE_CANCELED
            }
            if (bulkSize >= FORK_BULK_SIZE || i >= quantity - 1) {
                try {
                    context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                } catch (e: OperationApplicationException) {
                    e.printStackTrace()
                }

                operations.clear()
                bulkSize = 0
                val progress = i * 100 / quantity
                publishProgress(progress, i + 1)
            }
        }

        return TYPE_COMPLETED
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val progress = values[0]
        if (progress!! > mLastProgress) {
            mForkListener.onProgress(progress, values[1]!!)
            mLastProgress = progress
        }
    }

    override fun onPostExecute(status: Int?) {
        when (status) {
            TYPE_COMPLETED -> mForkListener.onCompleted()
            TYPE_CANCELED -> mForkListener.onCanceled()
            else -> mForkListener.onFailed()
        }
    }

    fun cancelFork() {
        mForkCanceled = true
    }

    companion object {
        private const val TAG = "ForkTask"

        const val TYPE_COMPLETED = 1
        const val TYPE_CANCELED = 2
        const val TYPE_FAILED = 3

        const val FORK_TYPE_RANDOM_CALLLOGS = 1
        const val FORK_TYPE_SPECIFIED_CALLLOGS = 2
        const val FORK_TYPE_RANDOM_CONTACT = 3
        const val FORK_TYPE_RANDOM_RCS_CALLLOGS = 4
        const val FORK_TYPE_SPECIFIED_RCS_CALLLOGS = 5
        const val FORK_TYPE_ALL_TYPE_CONTACT = 6
        const val FORK_TYPE_VVM = 7

        const val CONTACT_POSSIBLE_NUM_COUNT = 5

        private const val FORK_BULK_SIZE = 10
    }
}
