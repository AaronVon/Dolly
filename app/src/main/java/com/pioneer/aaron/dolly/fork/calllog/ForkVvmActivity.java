package com.pioneer.aaron.dolly.fork.calllog;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.pioneer.aaron.dolly.R;
import com.pioneer.aaron.dolly.utils.ExpandCollapseAnimation;

import me.yokeyword.fragmentation_swipeback.SwipeBackActivity;

/**
 * Created by Aaron on 11/8/17.
 */

public class ForkVvmActivity extends SwipeBackActivity implements IForkCallLogContract.View {
    private static final String TAG = "ForkVvmActivity";

    private IForkCallLogContract.Presenter mPresenter;

    private EditText mPhoneNumberEditText;
    private Button mStartForkButton;
    private ViewStub mAdvancedViewStub;
    private View mAdvancedView;
    private boolean mAdvancedOptViewOpened = false;
    private Button mStateButton;
    private Spinner mStateSpinner;

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

        mPhoneNumberEditText = (EditText) findViewById(R.id.call_log_number_edtxt);
        mStartForkButton = (Button) findViewById(R.id.start_fork_calllog_btn);
        mStartForkButton.setOnClickListener(mOnClickListener);
        mStartForkButton.setOnLongClickListener(mOnLongClickListener);
        mAdvancedViewStub = (ViewStub) findViewById(R.id.advanced_vvm_viewstub);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start_fork_calllog_btn:
                    mPresenter.forkVvmCallLog(ForkVvmActivity.this, mPhoneNumberEditText.getText().toString());
                    break;
                case R.id.state_btn:
                    mPresenter.sendVvmState(mStateSpinner.getSelectedItem().toString());
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
        mStateButton = (Button) mAdvancedView.findViewById(R.id.state_btn);
        mStateButton.setOnClickListener(mOnClickListener);
        mStateSpinner = (Spinner) mAdvancedView.findViewById(R.id.states_spinner);
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
