package com.pioneer.aaron.dolly.fork.calllog

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.view.ViewStub
import android.widget.*
import com.pioneer.aaron.dolly.R
import com.pioneer.aaron.dolly.utils.ExpandCollapseAnimation
import com.pioneer.aaron.dolly.utils.ForkConstants
import me.yokeyword.fragmentation_swipeback.SwipeBackActivity

/**
 * Created by Aaron on 11/8/17.
 */

class ForkVvmActivity : SwipeBackActivity(), IForkCallLogContract.View {
    companion object {
        private const val TAG = "ForkVvmActivity"
    }

    private lateinit var mPresenter: IForkCallLogContract.Presenter

    private  var mAdvancedOptViewOpened = false
    private lateinit var mPhoneNumberEditText: EditText
    private lateinit var mStartForkButton: Button
    private var mAdvancedViewStub: ViewStub? = null
    private var mAdvancedView: View? = null
    private lateinit var mConfigButton: Button
    private lateinit var mConfigSpinner: Spinner
    private lateinit var mDataButton: Button
    private lateinit var mDataSpinner: Spinner
    private lateinit var mNotificationButton: Button
    private lateinit var mNotificationSpinner: Spinner
    private lateinit var mSubscriptionRadioGroup: RadioGroup
    private lateinit var mSubOneRadioButton: RadioButton
    private lateinit var mSubTwoRadioButton: RadioButton

    private val mOnClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.start_fork_calllog_btn -> mPresenter.forkVvmCallLog(this@ForkVvmActivity, mPhoneNumberEditText.text.toString(),
                    if (mSubOneRadioButton.isChecked) ForkConstants.SIM_ONE else ForkConstants.SIM_TWO)
            R.id.config_btn -> mPresenter.sendVvmState(ForkCallLogPresenter.VVM_STATE.CONFIGURATION, mConfigSpinner.selectedItem.toString())
            R.id.data_btn -> mPresenter.sendVvmState(ForkCallLogPresenter.VVM_STATE.DATA, mDataSpinner.selectedItem.toString())
            R.id.notification_btn -> mPresenter.sendVvmState(ForkCallLogPresenter.VVM_STATE.NOTIFICATION, mNotificationSpinner.selectedItem.toString())
            else -> {
            }
        }
    }

    private val mOnLongClickListener = View.OnLongClickListener { v ->
        when (v.id) {
            R.id.start_fork_calllog_btn -> {
                mPresenter.vibrate()
                loadAdvancedView()
                animateAdvancedOptView(!mAdvancedOptViewOpened)
            }
            else -> {
            }
        }
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forkvvm)

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

        mPhoneNumberEditText = findViewById<View>(R.id.call_log_number_edtxt) as EditText
        mStartForkButton = findViewById<View>(R.id.start_fork_calllog_btn) as Button
        mStartForkButton.setOnClickListener(mOnClickListener)
        mStartForkButton.setOnLongClickListener(mOnLongClickListener)
        mAdvancedViewStub = findViewById<View>(R.id.advanced_vvm_viewstub) as ViewStub
        mSubscriptionRadioGroup = findViewById<View>(R.id.subscription_id_group) as RadioGroup
        mSubOneRadioButton = findViewById<View>(R.id.sub_one) as RadioButton
        mSubOneRadioButton.isChecked = true
        mSubTwoRadioButton = findViewById<View>(R.id.sub_two) as RadioButton
    }

    private fun loadAdvancedView() {
        if (mAdvancedViewStub == null) {
            Log.d(TAG, "AdvancedViewStub has been inflated.")
            return
        }

        mAdvancedView = mAdvancedViewStub!!.inflate()
        mAdvancedViewStub = null
        // configuration state
        mConfigButton = mAdvancedView?.findViewById<View>(R.id.config_btn) as Button
        mConfigButton.setOnClickListener(mOnClickListener)
        mConfigSpinner = mAdvancedView?.findViewById<View>(R.id.states_spinner) as Spinner

        // data state
        mDataButton = findViewById<View>(R.id.data_btn) as Button
        mDataButton.setOnClickListener(mOnClickListener)
        mDataSpinner = findViewById<View>(R.id.data_spinner) as Spinner

        // notification state
        mNotificationButton = findViewById<View>(R.id.notification_btn) as Button
        mNotificationButton.setOnClickListener(mOnClickListener)
        mNotificationSpinner = findViewById<View>(R.id.notification_spinner) as Spinner
    }

    /**
     * @param open TRUE to open, while FALSE to close.
     */
    private fun animateAdvancedOptView(open: Boolean) {
        if (mAdvancedView != null) {
            mAdvancedOptViewOpened = !mAdvancedOptViewOpened
            val animation = ExpandCollapseAnimation(mAdvancedView!!,
                    if (open) ExpandCollapseAnimation.EXPANEDED else ExpandCollapseAnimation.COLLAPSED)
            animation.duration = 200
            mAdvancedView!!.startAnimation(animation)
        } else {
            Log.d(TAG, "Failed to animate advanced opt view due to null object.")
        }
    }

    override fun toast(msg: String) {
        Snackbar.make(mStartForkButton, msg, Snackbar.LENGTH_SHORT).show()
    }
}
