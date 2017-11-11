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

    @Override
    public void toast(String msg) {
        Snackbar.make(mStartForkButton, msg, Snackbar.LENGTH_SHORT).show();
    }
}
