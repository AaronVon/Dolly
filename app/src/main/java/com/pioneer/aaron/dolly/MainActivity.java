package com.pioneer.aaron.dolly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.pioneer.aaron.dolly.fork.DataBaseOperator;
import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogActivity;
import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogData;
import com.pioneer.aaron.dolly.fork.calllog.ForkVvmActivity;
import com.pioneer.aaron.dolly.fork.contacts.ForkContactsActivity;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements IMainContract.View {

    private View mForkCallLogButton;
    private View mForkContactButton;
    private View mForkVvmButton;

    private IMainContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPresenter = new MainPresenter(this);

        mForkCallLogButton = (View) findViewById(R.id.fork_call_log_btn);
        mForkContactButton = (View) findViewById(R.id.fork_contact_btn);
        mForkVvmButton = (View) findViewById(R.id.fork_vvm_btn);
        mForkCallLogButton.setOnClickListener(mOnClickListener);
        mForkCallLogButton.setOnLongClickListener(mOnLongClickListener);
        mForkContactButton.setOnClickListener(mOnClickListener);
        mForkVvmButton.setOnClickListener(mOnClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.checkPermissions(this);
        mPresenter.loadResInBackground(this);
    }

    View.OnClickListener mOnClickListener = v -> {
        switch (v.getId()) {
            case R.id.fork_call_log_btn:
                Intent forkCallLogIntent = new Intent(MainActivity.this, ForkCallLogActivity.class);
                startActivity(forkCallLogIntent);
                break;
            case R.id.fork_contact_btn:
                Intent forkContactIntent = new Intent(MainActivity.this, ForkContactsActivity.class);
                startActivity(forkContactIntent);
                break;
            case R.id.fork_vvm_btn:
                Intent forkVvmIntent = new Intent(MainActivity.this, ForkVvmActivity.class);
                startActivity(forkVvmIntent);
                break;
            default:
                break;
        }
    };

    View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            HashMap<String, Boolean> columnExists = DataBaseOperator.getInstance(MainActivity.this).getColumnsExists();
            if (columnExists.get(ForkCallLogData.SUBJECT)
                    && columnExists.get(ForkCallLogData.POST_CALL_TEXT)
                    && columnExists.get(ForkCallLogData.IS_PRIMARY)) {
                mPresenter.vibrate();
                mPresenter.forkRCS(MainActivity.this);
            }
            return true;
        }
    };

    @Override
    protected void onDestroy() {
        mPresenter.onDestroy(this);
        super.onDestroy();
    }
}
