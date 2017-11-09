package com.pioneer.aaron.dolly.fork.calllog;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pioneer.aaron.dolly.R;

import me.yokeyword.fragmentation_swipeback.SwipeBackActivity;

/**
 * Created by Aaron on 11/8/17.
 */

public class ForkVvmActivity extends SwipeBackActivity implements IForkCallLogContract.View {

    private IForkCallLogContract.Presenter mPresenter;

    private EditText mPhoneNumberEditText;
    private Button mStartForkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forkvvm);

        mPresenter = new ForkCallLogPresenter(this);
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
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start_fork_calllog_btn:
                    mPresenter.forkVvmCallLog(ForkVvmActivity.this, mPhoneNumberEditText.getText().toString());
                    break;
                default:
                    break;
            }
        }
    };
}
