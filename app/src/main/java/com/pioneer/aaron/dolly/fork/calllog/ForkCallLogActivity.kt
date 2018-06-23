package com.pioneer.aaron.dolly.fork.calllog

import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CallLog
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.pioneer.aaron.dolly.R
import com.pioneer.aaron.dolly.fork.DataBaseOperator
import com.pioneer.aaron.dolly.utils.ForkConstants
import com.pioneer.aaron.dolly.utils.PermissionChecker
import me.yokeyword.fragmentation_swipeback.SwipeBackActivity
import java.util.*

/**
 * Created by Aaron on 4/18/17.
 */

class ForkCallLogActivity : SwipeBackActivity(), IForkCallLogContract.View {
    private lateinit var mPresenter: IForkCallLogContract.Presenter
    private lateinit var mPhoneNumberEditText: EditText
    private lateinit var mStartForkButton: Button
    private lateinit var mCallLogTypeGroup: RadioGroup
    private lateinit var mOutgoingRadioButton: RadioButton
    private lateinit var mRejectedRadioButton: RadioButton
    private lateinit var mIncomingRadioButton: RadioButton
    private lateinit var mMissedRadioButton: RadioButton

    private lateinit var mCallLogVolteGroup: RadioGroup
    private lateinit var mVolteRadioButton: RadioButton
    private lateinit var mVowifiRadioButton: RadioButton
    private lateinit var mHdRadioButton: RadioButton
    private lateinit var mNoneRadioButton: RadioButton

    private lateinit var mEncryptedCallCheckBox: CheckBox
    private lateinit var mVideoCallCheckBox: CheckBox
    private lateinit var mSubscriptionRadioGroup: RadioGroup
    private lateinit var mSubOneRadioButton: RadioButton
    private lateinit var mRollDiceCheckBox: CheckBox
    private lateinit var mCallLogQuantityEditText: EditText

    private lateinit var mColumnsExist: HashMap<String, Boolean>

    private var mOnClickListener = { v: View ->
        when (v.id) {
            R.id.start_fork_calllog_btn -> startForkCallLogs()
            else -> {
            }
        }
    }

    private val keyValuesToFork: ForkCallLogData
        get() {
            val data = ForkCallLogData()

            data.phoneNum = mPhoneNumberEditText.text.toString()

            var type = CallLog.Calls.INCOMING_TYPE
            if (mCallLogTypeGroup.visibility == View.VISIBLE) {
                type = when {
                    mOutgoingRadioButton.isChecked -> CallLog.Calls.OUTGOING_TYPE
                    mRejectedRadioButton.isChecked -> CallLog.Calls.REJECTED_TYPE
                    mIncomingRadioButton.isChecked -> CallLog.Calls.INCOMING_TYPE
                    else -> CallLog.Calls.MISSED_TYPE
                }
            }
            data.type = type

            var volte_type = 0
            if (mCallLogVolteGroup.visibility == View.VISIBLE) {
                if (mVolteRadioButton.isChecked) {
                    volte_type = 82
                } else if (mVowifiRadioButton.isChecked) {
                    volte_type = 83
                } else if (mHdRadioButton.isChecked) {
                    volte_type = 81
                } else if (mNoneRadioButton.isChecked) {
                    volte_type = 0
                }
            }
            data.callType = volte_type

            var encrypt_call = 0
            if (mEncryptedCallCheckBox.visibility == View.VISIBLE && mEncryptedCallCheckBox.isChecked) {
                encrypt_call = 1
            }
            data.enryptCall = encrypt_call

            var features = 0
            if (mVideoCallCheckBox.visibility == View.VISIBLE && mVideoCallCheckBox.isChecked) {
                features = 1
            }
            data.features = features

            data.subId = if (mSubOneRadioButton.isChecked) ForkConstants.SIM_ONE else ForkConstants.SIM_TWO

            data.quantity = Integer.parseInt(mCallLogQuantityEditText.text.toString())
            return data
        }

    private var mCheckedChangeListener = { buttonView: View, _: Boolean ->
        when (buttonView.id) {
            R.id.call_log_roll_dice -> updateButtonsStates()
            else -> {
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionChecker.PERMISSION_REQUEST_CODE -> if (grantResults.size > 0) {
                var allPermissionGranted = true
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionGranted = false
                        break
                    }
                }
                if (allPermissionGranted) {
                    initUI()
                }
            }
            else -> {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forkcalllog)

        mPresenter = ForkCallLogPresenter(this, this)
        mPresenter!!.loadResInBackground(this)
        if (mPresenter!!.checkPermissions(this)) {
            initUI()
        }
    }

    private fun initUI() {
        if (isTaskRoot) {
            setSwipeBackEnable(false)
        }
        mColumnsExist = mPresenter.getColumnsExist(applicationContext)

        mPhoneNumberEditText = findViewById<View>(R.id.call_log_number_edtxt) as EditText
        mStartForkButton = findViewById<View>(R.id.start_fork_calllog_btn) as Button
        mStartForkButton.setOnClickListener(mOnClickListener)

        mCallLogTypeGroup = findViewById<View>(R.id.call_log_type_radioGroup) as RadioGroup
        mOutgoingRadioButton = findViewById<View>(R.id.outgoing_radiobtn) as RadioButton
        mOutgoingRadioButton.isChecked = true

        mRejectedRadioButton = findViewById<View>(R.id.rejected_radiobtn) as RadioButton
        mIncomingRadioButton = findViewById<View>(R.id.answered_radiobtn) as RadioButton
        mMissedRadioButton = findViewById<View>(R.id.missed_radiobtn) as RadioButton

        mCallLogVolteGroup = findViewById<View>(R.id.call_log_volte_feature_radiogroup) as RadioGroup
        if (mColumnsExist[DataBaseOperator.CALLLOG_CALL_TYPE]!!) {
            mCallLogVolteGroup.visibility = View.VISIBLE
            mVolteRadioButton = findViewById<View>(R.id.call_log_volte_radiobtn) as RadioButton
            mVolteRadioButton.isChecked = true
            mVowifiRadioButton = findViewById<View>(R.id.call_log_vowifi_hd_radiobtn) as RadioButton
            mHdRadioButton = findViewById<View>(R.id.call_log_volte_hd_radiobtn) as RadioButton
            mNoneRadioButton = findViewById<View>(R.id.call_log_volte_none_radiobtn) as RadioButton
        } else {
            mCallLogVolteGroup.visibility = View.GONE
        }

        mEncryptedCallCheckBox = findViewById<View>(R.id.encrypted_call_chkbox) as CheckBox
        if (mColumnsExist[DataBaseOperator.CALLLOG_ENCRYPT]!!) {
            mEncryptedCallCheckBox.visibility = View.VISIBLE
            mEncryptedCallCheckBox.isChecked = true
        } else {
            mEncryptedCallCheckBox.visibility = View.GONE
        }

        mVideoCallCheckBox = findViewById<View>(R.id.video_call_chkbox) as CheckBox
        if (mColumnsExist[DataBaseOperator.CALLLOG_FEATURE]!!) {
            mVideoCallCheckBox.visibility = View.VISIBLE
        } else {
            mVideoCallCheckBox.visibility = View.GONE
        }

        mSubscriptionRadioGroup = findViewById<View>(R.id.subscription_id_group) as RadioGroup
        mSubOneRadioButton = findViewById<View>(R.id.sub_one) as RadioButton

        mRollDiceCheckBox = findViewById<View>(R.id.call_log_roll_dice) as CheckBox
        mRollDiceCheckBox.setOnCheckedChangeListener(mCheckedChangeListener)
        mCallLogQuantityEditText = findViewById<View>(R.id.call_log_quantity_edtxt) as EditText
    }

    override fun onResume() {
        super.onResume()
    }

    private fun startForkCallLogs() {
        val quantity = mCallLogQuantityEditText.text.toString()
        if (TextUtils.isEmpty(quantity) || Integer.valueOf(quantity) <= 0) {
            Snackbar.make(findViewById(R.id.activity_fork_call_log_layout),
                    R.string.call_log_quantity_msg, Snackbar.LENGTH_SHORT).show()
            return
        }
        if (!mRollDiceCheckBox.isChecked) {
            mPresenter.forkSpecifiedCallLog(applicationContext, keyValuesToFork)
        } else {
            mPresenter.forkRandomCallLogs(applicationContext, Integer.parseInt(mCallLogQuantityEditText.text.toString()))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.call_log_rcs -> {
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateButtonsStates() {
        if (mRollDiceCheckBox.isChecked) {
            setButtonsEnabled(false)
        } else {
            setButtonsEnabled(true)
        }
    }

    private fun setButtonsEnabled(isEnabled: Boolean) {
        mPhoneNumberEditText.isEnabled = isEnabled
        // call log types
        val calllogTypeSize = mCallLogTypeGroup.childCount
        for (i in 0 until calllogTypeSize) {
            mCallLogTypeGroup.getChildAt(i).isEnabled = isEnabled
        }

        // call log volte types
        val calllogVolteTypeSize = mCallLogVolteGroup.childCount
        for (i in 0 until calllogVolteTypeSize) {
            mCallLogVolteGroup.getChildAt(i).isEnabled = isEnabled
        }

        // subscription
        val subscriptionSize = mSubscriptionRadioGroup.childCount
        for (i in 0 until subscriptionSize) {
            mSubscriptionRadioGroup.getChildAt(i).isEnabled = isEnabled
        }

        mEncryptedCallCheckBox.isEnabled = isEnabled
        mVideoCallCheckBox.isEnabled = isEnabled
    }

    override fun onDestroy() {
        mPresenter.onDestroy(this)
        super.onDestroy()
    }

    override fun toast(msg: String) {

    }

    companion object {
        private val TAG = "ForkCallLogActivity"
    }
}
