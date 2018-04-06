package com.pioneer.aaron.dolly.fork.calllog;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.pioneer.aaron.dolly.R;
import com.pioneer.aaron.dolly.fork.DataBaseOperator;
import com.pioneer.aaron.dolly.utils.ExpandCollapseAnimation;
import com.pioneer.aaron.dolly.utils.ForkConstants;

import java.util.HashMap;

import me.yokeyword.fragmentation_swipeback.SwipeBackActivity;

/**
 * Created by Aaron on 11/8/17.
 */

public class ForkVvmActivity extends SwipeBackActivity implements IForkCallLogContract.View {
    private static final String TAG = "ForkVvmActivity";

    private IForkCallLogContract.Presenter mPresenter;
    private HashMap<String, Boolean> mColumnsExist;

    private EditText mPhoneNumberEditText;
    private Button mStartForkButton;
    private ViewStub mAdvancedViewStub;
    private View mAdvancedView;
    private boolean mAdvancedOptViewOpened = false;
    private Button mConfigButton;
    private Spinner mConfigSpinner;
    private Button mDataButton;
    private Spinner mDataSpinner;
    private Button mNotificationButton;
    private Spinner mNotificationSpinner;
    private RadioGroup mSubscriptionRadioGroup;
    private RadioButton mSubOneRadioButton;
    private RadioButton mSubTwoRadioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forkvvm);

        mPresenter = new ForkCallLogPresenter(this, this);
        mPresenter.loadResInBackground(this);
        if (mPresenter.checkPermissions(this)) {
            initUI();
        }
    }

    private void initUI() {
        if (isTaskRoot()) {
            setSwipeBackEnable(false);
        }
        mColumnsExist = mPresenter.getColumnsExist(this);

        mPhoneNumberEditText = (EditText) findViewById(R.id.call_log_number_edtxt);
        mStartForkButton = (Button) findViewById(R.id.start_fork_calllog_btn);
        mStartForkButton.setOnClickListener(mOnClickListener);
        mStartForkButton.setOnLongClickListener(mOnLongClickListener);
        mAdvancedViewStub = (ViewStub) findViewById(R.id.advanced_vvm_viewstub);
        mSubscriptionRadioGroup = (RadioGroup) findViewById(R.id.subscription_id_group);
        mSubOneRadioButton = (RadioButton) findViewById(R.id.sub_one);
        mSubOneRadioButton.setChecked(true);
        mSubTwoRadioButton = (RadioButton) findViewById(R.id.sub_two);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start_fork_calllog_btn:
                    mPresenter.forkVvmCallLog(ForkVvmActivity.this, mPhoneNumberEditText.getText().toString(),
                            mSubOneRadioButton.isChecked() ? ForkConstants.SIM_ONE : ForkConstants.SIM_TWO);
                    break;
                case R.id.config_btn:
                    mPresenter.sendVvmState(ForkCallLogPresenter.VVM_STATE.CONFIGURATION, mConfigSpinner.getSelectedItem().toString());
                    break;
                case R.id.data_btn:
                    mPresenter.sendVvmState(ForkCallLogPresenter.VVM_STATE.DATA, mDataSpinner.getSelectedItem().toString());
                    break;
                case R.id.notification_btn:
                    mPresenter.sendVvmState(ForkCallLogPresenter.VVM_STATE.NOTIFICATION, mNotificationSpinner.getSelectedItem().toString());
                    break;
                default:
                    break;
            }
        }
    };

    private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.start_fork_calllog_btn:
                    mPresenter.vibrate();
                    loadAdvancedView();
                    animateAdancedOptView(!mAdvancedOptViewOpened);
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private void loadAdvancedView() {
        if (mAdvancedViewStub == null) {
            Log.d(TAG, "AdvancedViewStub has been inflated.");
            return;
        }

        mAdvancedView = mAdvancedViewStub.inflate();
        mAdvancedViewStub = null;
        // configuration state
        mConfigButton = (Button) mAdvancedView.findViewById(R.id.config_btn);
        mConfigButton.setOnClickListener(mOnClickListener);
        mConfigSpinner = (Spinner) mAdvancedView.findViewById(R.id.states_spinner);

        // data state
        mDataButton = (Button) findViewById(R.id.data_btn);
        mDataButton.setOnClickListener(mOnClickListener);
        mDataSpinner = (Spinner) findViewById(R.id.data_spinner);

        // notification state
        mNotificationButton = (Button) findViewById(R.id.notification_btn);
        mNotificationButton.setOnClickListener(mOnClickListener);
        mNotificationSpinner = (Spinner) findViewById(R.id.notification_spinner);
    }

    /**
     * @param open TRUE to open, while FALSE to close.
     */
    private void animateAdancedOptView(boolean open) {
        if (mAdvancedView != null) {
            mAdvancedOptViewOpened = !mAdvancedOptViewOpened;
            ExpandCollapseAnimation animation = new ExpandCollapseAnimation(mAdvancedView,
                    open ? ExpandCollapseAnimation.EXPANEDED : ExpandCollapseAnimation.COLLAPSED);
            animation.setDuration(200);
            mAdvancedView.startAnimation(animation);
        } else {
            Log.d(TAG, "Failed to animate advanced opt view due to null object.");
        }
    }

    @Override
    public void toast(String msg) {
        Snackbar.make(mStartForkButton, msg, Snackbar.LENGTH_SHORT).show();
    }
}
