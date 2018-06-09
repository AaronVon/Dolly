package com.pioneer.aaron.dolly

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.provider.CallLog
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup

import com.pioneer.aaron.dolly.fork.ForkService
import com.pioneer.aaron.dolly.fork.ForkTask
import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogData
import com.pioneer.aaron.dolly.utils.ForkConstants
import com.pioneer.aaron.dolly.utils.ForkVibrator
import com.pioneer.aaron.dolly.utils.Matrix
import com.pioneer.aaron.dolly.utils.PermissionChecker

import java.lang.ref.WeakReference

/**
 * Created by Aaron on 4/18/17.
 */

class MainPresenter(mContext: Context) : IMainContract.Presenter {
    private lateinit var mForkBinder: ForkService.ForkBinder
    private val mHandler: MainHandler

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mForkBinder = service as ForkService.ForkBinder
        }

        override fun onServiceDisconnected(name: ComponentName) {

        }
    }

    private class MainHandler internal constructor(context: Context) : Handler() {
        private val weakReference: WeakReference<Context> = WeakReference(context)

        override fun handleMessage(msg: Message) {
            val context = weakReference.get()
            if (context == null) {
                Log.i(TAG, "MainHandler failed to handle msg due to context is NULL")
                return
            }
            when (msg.what) {
                ForkConstants.VIBRATE_ON_LONG_CLICK -> ForkVibrator.getInstance(context)?.vibrate(70)
                else -> {
                }
            }
        }
    }

    init {
        val intent = Intent(mContext, ForkService::class.java)
        mContext.startService(intent)
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        mHandler = MainHandler(mContext)
    }

    override fun checkPermissions(activity: Activity): Boolean {
        return PermissionChecker.checkPermissions(activity)
    }

    override fun loadResInBackground(context: Context) {
        Matrix.LoadResAsyncTask(context).execute(Matrix.LoadResAsyncTask.RES_TYPE_LOAD.ASSET_RES)
    }

    override fun forkRCS(context: Context) {
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.fork_rcs_calllog_layout, null)
        val numberEditText = view.findViewById<View>(R.id.rcs_call_log_number) as EditText
        val subjectEditText = view.findViewById<View>(R.id.rcs_call_log_subject) as EditText
        val postCallEditText = view.findViewById<View>(R.id.rcs_call_log_post_call_text) as EditText
        val quantityEditText = view.findViewById<View>(R.id.rcs_call_log_quantity) as EditText
        val typeRadioGroup = view.findViewById<View>(R.id.call_log_type_radioGroup) as RadioGroup
        val type = getTypeChecked(view)


        quantityEditText.setText(CALLLOG_DEFAULT_QUANTITY.toString())
        val rollDiceCheckBox = view.findViewById<View>(R.id.call_log_roll_dice) as CheckBox
        rollDiceCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            val enabled = !isChecked
            numberEditText.isEnabled = enabled
            subjectEditText.isEnabled = enabled
            postCallEditText.isEnabled = enabled
            val childSize = typeRadioGroup.childCount
            for (i in 0 until childSize) {
                val child = typeRadioGroup.getChildAt(i) as RadioButton
                child.isEnabled = enabled
            }
        }

        builder.setTitle(R.string.menu_call_log_rcs)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.start_fork_call_logs) { dialog, _ ->
                    if (rollDiceCheckBox.isChecked) {
                        startForkRandomRCS(Integer.parseInt(quantityEditText.text.toString()))
                    } else {
                        val data = ForkCallLogData()
                        data.phoneNum = numberEditText.text.toString()
                        data.type = type
                        data.subject = subjectEditText.text.toString()
                        data.postCallText = postCallEditText.text.toString()
                        data.quantity = Integer.parseInt(quantityEditText.text.toString())
                        startForkSpecifiedRCS(data)
                    }
                }
                .setView(view)
        val dialog = builder.create()
        dialog.show()
    }

    private fun getTypeChecked(view: View): Int {
        val outgoingRadioButton = view.findViewById<View>(R.id.outgoing_radiobtn) as RadioButton
        outgoingRadioButton.isChecked = true
        val rejectedRadioButton = view.findViewById<View>(R.id.rejected_radiobtn) as RadioButton
        val incomingRadioButton = view.findViewById<View>(R.id.incoming_radiobtn) as RadioButton
        return when {
            outgoingRadioButton.isChecked -> CallLog.Calls.OUTGOING_TYPE
            rejectedRadioButton.isChecked -> CallLog.Calls.REJECTED_TYPE
            incomingRadioButton.isChecked -> CallLog.Calls.INCOMING_TYPE
            else -> CallLog.Calls.MISSED_TYPE
        }
    }

    override fun onDestroy(context: Context) {
        context.unbindService(mServiceConnection)
    }

    override fun vibrate() {
        mHandler.sendEmptyMessage(ForkConstants.VIBRATE_ON_LONG_CLICK)
    }

    private fun startForkRandomRCS(quantity: Int) {
        mForkBinder.startFork(ForkTask.FORK_TYPE_RANDOM_RCS_CALLLOGS, quantity)
    }

    private fun startForkSpecifiedRCS(data: ForkCallLogData) {
        mForkBinder.startFork(ForkTask.FORK_TYPE_SPECIFIED_RCS_CALLLOGS, data.quantity, data)
    }

    companion object {
        private val TAG = "MainPresenter"

        private val CALLLOG_DEFAULT_QUANTITY = 5
    }
}
