package com.pioneer.aaron.dolly.fork.contacts;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.pioneer.aaron.dolly.R;
import com.pioneer.aaron.dolly.utils.PermissionChecker;

/**
 * Created by Aaron on 4/18/17.
 */

public class ForkContactsActivity extends AppCompatActivity {

    private IForkContactContract.Presenter mPresenter;
    private static final int CONTACT_DEFAULT_QUANTITY = 20;

    private EditText mContactQuantity;
    private Button mStartForkButton;
    private CheckBox mContactsAllTypeCheckBox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forkcontacts);

        mPresenter = new ForkContactPresenter(this);
        if (mPresenter.checkPermissions(this)) {
            initUI();
        }
    }

    private void initUI() {
        mContactQuantity = (EditText) findViewById(R.id.contact_quantity_edtxt);
        mContactQuantity.setText(String.valueOf(CONTACT_DEFAULT_QUANTITY));

        mStartForkButton = (Button) findViewById(R.id.start_fork_contact_btn);
        mStartForkButton.setOnClickListener(mOnClickListener);

        mContactsAllTypeCheckBox = (CheckBox) findViewById(R.id.fork_contact_all_type);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionChecker.PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean allPermissionGranted = true;
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            allPermissionGranted = false;
                            break;
                        }
                    }
                    if (allPermissionGranted) {
                        initUI();
                    }
                }
                break;
            default:
                break;
        }

    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start_fork_contact_btn:
                    startFork(mContactsAllTypeCheckBox.isChecked());
                    break;
                default:
                    break;
            }
        }
    };

    private void startFork(boolean allTypes) {
        int quantity = Integer.parseInt(mContactQuantity.getText().toString());
        if (quantity <= 0) {
            Snackbar.make(findViewById(R.id.activity_fork_contact_layout), R.string.contact_quantity_msg, Snackbar.LENGTH_SHORT)
                    .show();
        } else {
            mPresenter.forkContacts(this, quantity, allTypes);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy(this);
    }
}
