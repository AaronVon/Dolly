package com.pioneer.aaron.dolly.fork.calllog

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.provider.VoicemailContract
import android.util.Log
import com.pioneer.aaron.dolly.R
import com.pioneer.aaron.dolly.fork.DataBaseOperator
import com.pioneer.aaron.dolly.fork.ForkService
import com.pioneer.aaron.dolly.fork.ForkTask
import com.pioneer.aaron.dolly.utils.ForkConstants
import com.pioneer.aaron.dolly.utils.ForkVibrator
import com.pioneer.aaron.dolly.utils.Matrix
import com.pioneer.aaron.dolly.utils.PermissionChecker
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Aaron on 4/28/17.
 */

class ForkCallLogPresenter(private val mContext: Context, private val mView: IForkCallLogContract.View) : IForkCallLogContract.Presenter {
    private val mHandler: MyHandler
    private lateinit var mForkBinder: ForkService.ForkBinder

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mForkBinder = service as ForkService.ForkBinder
        }

        override fun onServiceDisconnected(name: ComponentName) {

        }
    }

    init {
        val intent = Intent(mContext, ForkService::class.java)
        mContext.startService(intent)
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        mHandler = MyHandler(mContext)
    }

    private class MyHandler internal constructor(context: Context) : Handler() {
        private val weakRef: WeakReference<Context> = WeakReference(context)

        override fun handleMessage(msg: Message) {
            weakRef.get()?.let {
                when (msg.what) {
                    ForkConstants.VIBRATE_ON_LONG_CLICK -> ForkVibrator.getInstance(it)!!.vibrate(70)
                    else -> {
                    }
                }
            } ?: Log.i(TAG, "MyHandler failed to handleMessage due to context is NULL")
        }
    }

    override fun getColumnsExist(context: Context): HashMap<String, Boolean> {
        return DataBaseOperator.getInstance(context).columnsExists
    }

    override fun forkRandomCallLogs(context: Context, quantity: Int) {
        mForkBinder.startFork(ForkTask.FORK_TYPE_RANDOM_CALLLOGS, quantity)
    }

    override fun forkSpecifiedCallLog(context: Context, data: ForkCallLogData) {
        mForkBinder.startFork(ForkTask.FORK_TYPE_SPECIFIED_CALLLOGS, data.quantity, data)
    }

    override fun forkVvmCallLog(context: Context, phoneNumber: String, subId: Int) {
        mForkBinder.startFork(ForkTask.FORK_TYPE_VVM, phoneNumber, subId)
    }

    override fun sendVvmState(vvmState: String, state: String) {
        ConfigStateAsyncTask(this).execute(vvmState, state)
    }

    class ConfigStateAsyncTask(presenter: ForkCallLogPresenter) : AsyncTask<String, Void, Void>() {
        private val weakReference: WeakReference<ForkCallLogPresenter> = WeakReference(presenter)
        private var mConfigState = ForkConstants.INVALID_CONFIG_STATE
        private var state: String? = null
        private var vvmStateClassification: String? = null

        override fun doInBackground(vararg params: String): Void? {
            val context = weakReference.get()?.mContext ?: return null
            // param[0] is VVM_STATE;
            // param[1] is state value;
            var configs: Array<String>? = null
            vvmStateClassification = params[0]
            when (params[0]) {
                VVM_STATE.CONFIGURATION -> configs = context.resources.getStringArray(R.array.vvm_config_states)
                VVM_STATE.DATA -> configs = context.resources.getStringArray(R.array.vvm_data_state)
                VVM_STATE.NOTIFICATION -> configs = context.resources.getStringArray(R.array.vvm_notification)
                else -> {
                }
            }
            if (configs == null) {
                Log.i(TAG, "configs is NULL")
                return null
            }
            state = params[1]
            for (i in configs.indices) {
                if (configs[i] == state) {
                    mConfigState = i
                    break
                }
            }
            val contentResolver = context.contentResolver
            val configUri = VoicemailContract.Status.buildSourceUri(context.packageName)

            val contentValues = ContentValues()
            when (params[0]) {
                VVM_STATE.CONFIGURATION -> contentValues.put(VoicemailContract.Status.CONFIGURATION_STATE, mConfigState)
                VVM_STATE.DATA -> contentValues.put(VoicemailContract.Status.DATA_CHANNEL_STATE, mConfigState)
                VVM_STATE.NOTIFICATION -> contentValues.put(VoicemailContract.Status.NOTIFICATION_CHANNEL_STATE, mConfigState)
                else -> {
                }
            }
            contentValues.put(VoicemailContract.Status.VOICEMAIL_ACCESS_URI, "tel:888")
            contentValues.put(VoicemailContract.Status.SETTINGS_URI, Uri.decode("http://www.default.config.com"))
            contentValues.put(VoicemailContract.Status.SOURCE_PACKAGE, context.packageName)

            contentResolver.insert(configUri, contentValues)
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            if (mConfigState != ForkConstants.INVALID_CONFIG_STATE) {
                Log.i(TAG, "$vvmStateClassification $mConfigState was sent.")
                weakReference.get()?.toast("$vvmStateClassification $state was sent.")
            }
        }
    }

    override fun vibrate() {
        mHandler.sendEmptyMessage(ForkConstants.VIBRATE_ON_LONG_CLICK)
    }

    override fun toast(msg: String) {
        mView.toast(msg)
    }

    override fun checkPermissions(activity: Activity): Boolean {
        return PermissionChecker.checkPermissions(activity)
    }

    override fun loadResInBackground(context: Context) {
        // load existing contact phone number
        Matrix.LoadResAsyncTask(context).execute(Matrix.LoadResAsyncTask.RES_TYPE_LOAD.CONTACT_NUMBRES)
    }

    override fun onDestroy(context: Context) {
        try {
            context.unbindService(mServiceConnection)
        } catch (ise: IllegalStateException) {
            Log.e(TAG, "onDestroy: unbindService IllegalStateException")
        }

    }

    interface VVM_STATE {
        companion object {
            const val CONFIGURATION = "CONFIGURATION"
            const val NOTIFICATION = "NOTIFICATION"
            const val DATA = "DATA"
        }
    }

    companion object {
        private const val TAG = "ForkCallLogPresenter"
    }
}
