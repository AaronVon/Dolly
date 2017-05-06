package com.pioneer.aaron.dolly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.pioneer.aaron.dolly.fork.DataBaseOpearator;
import com.pioneer.aaron.dolly.fork.ForkTask;
import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogActivity;
import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogData;
import com.pioneer.aaron.dolly.fork.contacts.ForkContactsActivity;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements IMainContract.View {

    private Button mForkCallLogButton;
    private Button mForkContactButton;

    private IMainContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPresenter = new MainPresenter(this);

        mForkCallLogButton = (Button) findViewById(R.id.fork_calllog_btn);
        mForkContactButton = (Button) findViewById(R.id.fork_contact_btn);
        mForkCallLogButton.setOnClickListener(mOnClickListener);
        mForkCallLogButton.setOnLongClickListener(mOnLongClickListener);
        mForkContactButton.setOnClickListener(mOnClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.checkPermissions(this);
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fork_calllog_btn:
                    Intent forkCallLogIntent = new Intent(MainActivity.this, ForkCallLogActivity.class);
                    startActivity(forkCallLogIntent);
                    break;
                case R.id.fork_contact_btn:
                    Intent forkContactIntent = new Intent(MainActivity.this, ForkContactsActivity.class);
                    startActivity(forkContactIntent);
                    break;
                default:
                    break;
            }
        }
    };

    View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            HashMap<String, Boolean> columnExists = DataBaseOpearator.getInstance(MainActivity.this).getColumnsExists();
            if (columnExists.containsKey(ForkCallLogData.SUBJECT)
                    && columnExists.containsKey(ForkCallLogData.POST_CALL_TEXT)
                    && columnExists.containsKey(ForkCallLogData.IS_PRIMARY)) {
                mPresenter.forkRCS(MainActivity.this);
            }
            return false;
        }
    };

    @Override
    protected void onDestroy() {
        mPresenter.onDestroy(this);
        super.onDestroy();
    }
}
