package com.pioneer.aaron.dolly.fork.calllog

import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CallLog
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.InputType
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.ViewManager
import android.widget.*
import anko.Id
import anko.immersiveToolbar
import anko.subscriptionLayout
import com.pioneer.aaron.dolly.R
import com.pioneer.aaron.dolly.fork.DataBaseOperator
import com.pioneer.aaron.dolly.utils.ForkConstants
import com.pioneer.aaron.dolly.utils.PermissionChecker
import com.pioneer.aaron.dolly.utils.PreferenceHelper
import me.yokeyword.fragmentation_swipeback.SwipeBackActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
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
    private lateinit var mRootView: View

    private lateinit var mColumnsExist: HashMap<String, Boolean>

    private var mOnClickListener = { v: View ->
        when (v) {
            mStartForkButton -> startForkCallLogs()
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

            var volteType = 0
            if (mCallLogVolteGroup.visibility == View.VISIBLE) {
                when {
                    mVolteRadioButton.isChecked -> volteType = 82
                    mVowifiRadioButton.isChecked -> volteType = 83
                    mHdRadioButton.isChecked -> volteType = 81
                    mNoneRadioButton.isChecked -> volteType = 0
                }
            }
            data.callType = volteType

            var encryptCall = 0
            if (mEncryptedCallCheckBox.visibility == View.VISIBLE && mEncryptedCallCheckBox.isChecked) {
                encryptCall = 1
            }
            data.enryptCall = encryptCall

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
        when (buttonView) {
            mRollDiceCheckBox -> updateButtonsStates()
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
        anko.makeImmersive(window)
        ForkCallLogActivityUI().setContentView(this)

        mPresenter = ForkCallLogPresenter(this, this)
        mPresenter.loadResInBackground(this)
        if (mPresenter.checkPermissions(this)) {
            initUI()
        }
    }

    private fun initUI() {
        if (isTaskRoot) {
            setSwipeBackEnable(false)
        }
        mColumnsExist = mPresenter.getColumnsExist(applicationContext)

        if (mColumnsExist[DataBaseOperator.CALLLOG_CALL_TYPE]!!) {
            mCallLogVolteGroup.visibility = View.VISIBLE
            mVolteRadioButton.isChecked = true
        } else {
            mCallLogVolteGroup.visibility = View.GONE
        }

        if (mColumnsExist[DataBaseOperator.CALLLOG_ENCRYPT]!!) {
            mEncryptedCallCheckBox.visibility = View.VISIBLE
            mEncryptedCallCheckBox.isChecked = true
        } else {
            mEncryptedCallCheckBox.visibility = View.GONE
        }

        mVideoCallCheckBox.visibility = if (mColumnsExist[DataBaseOperator.CALLLOG_FEATURE]!!) {
            View.VISIBLE
        } else {
            View.GONE
        }

        mSubOneRadioButton = findViewById<View>(Id.subOneRadioButton) as RadioButton

        doAsync {
            val forkCallLogsData = PreferenceHelper.getInstance(application).mForkCallLogsData
            uiThread {
                mCallLogQuantityEditText.setText(forkCallLogsData.quantity.toString())
                mRollDiceCheckBox.isChecked = forkCallLogsData.allRandom
            }
        }
    }

    private fun startForkCallLogs() {
        val quantity = mCallLogQuantityEditText.text.toString()
        if (TextUtils.isEmpty(quantity) || Integer.valueOf(quantity) <= 0) {
            Snackbar.make(mRootView,
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
        private val TAG = ForkCallLogActivity::class.java.simpleName
    }

    inner class ForkCallLogActivityUI : AnkoComponent<ForkCallLogActivity> {
        inline fun ViewManager.textInputEditText(theme: Int = 0, init: TextInputEditText.() -> Unit) = ankoView({ TextInputEditText(it) }, theme, init)

        inline fun ViewManager.textInputLayout(theme: Int = 0, init: TextInputLayout.() -> Unit) = ankoView(::TextInputLayout, theme, init)

        override fun createView(ui: AnkoContext<ForkCallLogActivity>): View =
                with(ui) {
                    mRootView = verticalLayout {
                        immersiveToolbar(this@ForkCallLogActivity)
                        scrollView {
                            verticalLayout {
                                padding = dimen(R.dimen.activity_horizontal_margin)
                                verticalLayout {
                                    frameLayout {
                                        textInputLayout {
                                            mPhoneNumberEditText = textInputEditText {
                                                hint = resources.getString(R.string.calllog_number_hint)
                                                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
                                            }.lparams(width = matchParent)
                                        }.lparams(width = matchParent)
                                    }
                                    mCallLogTypeGroup = radioGroup {
                                        orientation = LinearLayout.HORIZONTAL
                                        mOutgoingRadioButton = radioButton {
                                            isChecked = true
                                            ellipsize = TextUtils.TruncateAt.END
                                            maxLines = 1
                                            text = resources.getString(R.string.call_log_outgoing)
                                            allCaps = true
                                        }.lparams {
                                            weight = 1f
                                        }
                                        mRejectedRadioButton = radioButton {
                                            ellipsize = TextUtils.TruncateAt.END
                                            maxLines = 1
                                            text = resources.getString(R.string.call_log_rejected)
                                            allCaps = true
                                        }.lparams {
                                            weight = 1f
                                        }
                                        mIncomingRadioButton = radioButton {
                                            ellipsize = TextUtils.TruncateAt.END
                                            maxLines = 1
                                            text = resources.getString(R.string.call_log_incoming)
                                            allCaps = true
                                        }.lparams {
                                            weight = 1f
                                        }
                                        mMissedRadioButton = radioButton {
                                            ellipsize = TextUtils.TruncateAt.END
                                            maxLines = 1
                                            text = resources.getString(R.string.call_log_missed)
                                            allCaps = true
                                        }.lparams {
                                            weight = 1f
                                        }
                                    }.lparams(width = matchParent)
                                    mCallLogVolteGroup = radioGroup {
                                        isBaselineAligned = false
                                        orientation = LinearLayout.HORIZONTAL
                                        visibility = View.GONE
                                        mVolteRadioButton = radioButton {
                                            ellipsize = TextUtils.TruncateAt.END
                                            maxLines = 1
                                            text = resources.getString(R.string.call_log_volte)
                                            allCaps = true
                                        }
                                        mHdRadioButton = radioButton {
                                            //android:ellipsize = end //not support attribute
                                            ellipsize = TextUtils.TruncateAt.END
                                            maxLines = 1
                                            text = resources.getString(R.string.call_log_volte_hd)
                                            allCaps = false
                                        }
                                        mVowifiRadioButton = radioButton {
                                            ellipsize = TextUtils.TruncateAt.END
                                            maxLines = 1
                                            text = resources.getString(R.string.call_log_vowifi)
                                            allCaps = false
                                        }
                                        mNoneRadioButton = radioButton {
                                            ellipsize = TextUtils.TruncateAt.END
                                            maxLines = 1
                                            text = resources.getString(R.string.call_log_volte_none)
                                            allCaps = false
                                        }
                                    }
                                    mEncryptedCallCheckBox = checkBox {
                                        text = resources.getString(R.string.call_log_encrypt)
                                        allCaps = false
                                        visibility = View.GONE
                                    }
                                    mVideoCallCheckBox = checkBox {
                                        text = resources.getString(R.string.call_log_video)
                                        allCaps = false
                                        visibility = View.GONE
                                    }
                                }.lparams(width = matchParent)

                                mSubscriptionRadioGroup = subscriptionLayout(context)

                                mRollDiceCheckBox = checkBox {
                                    text = resources.getString(R.string.call_log_roll_dice)
                                    setOnCheckedChangeListener(mCheckedChangeListener)
                                }
                                frameLayout {
                                    textInputLayout {
                                        mCallLogQuantityEditText = editText {
                                            hint = resources.getString(R.string.calllog_quantity_hint)
                                            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
                                        }.lparams(width = matchParent)
                                    }.lparams(width = matchParent)
                                }
                                mStartForkButton = button {
                                    text = resources.getString(R.string.start_fork_call_logs)
                                    allCaps = false
                                    setOnClickListener(mOnClickListener)
                                }.lparams(width = matchParent)
                            }.lparams(width = matchParent)
                        }
                    }
                    return mRootView
                }
    }
}
