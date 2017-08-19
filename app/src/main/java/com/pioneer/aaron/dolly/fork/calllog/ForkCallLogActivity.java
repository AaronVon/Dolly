package com.pioneer.aaron.dolly.fork.calllog;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pioneer.aaron.dolly.R;
import com.pioneer.aaron.dolly.fork.DataBaseOperator;
import com.pioneer.aaron.dolly.utils.PermissionChecker;

import java.util.HashMap;

/**
 * Created by Aaron on 4/18/17.
 */

public class ForkCallLogActivity extends AppCompatActivity implements IForkCallLogContract.View {
    private static final String TAG = "Aaron";
    private IForkCallLogContract.Presenter mPresenter;

    private static final int CALLLOG_DEFAULT_QUANTITY = 5;
    EditText mPhoneNumberEditText;
    Button mStartForkButton;
    RadioGroup mCallLogTypeGroup;
    RadioButton mOutgoingRadioButton;
    RadioButton mRejectedRadioButton;
    RadioButton mIncomingRadioButton;
    RadioButton mMissedRadioButton;

    RadioGroup mCallLogVolteGroup;
    RadioButton mVolteRadioButton;
    RadioButton mVowifiRadioButton;
    RadioButton mHdRadioButton;
    RadioButton mNoneRadioButton;

    CheckBox mEncryptedCallCheckBox;
    CheckBox mVideoCallCheckBox;
    CheckBox mRollDiceCheckBox;
    EditText mCallLogQuantityEditText;

    HashMap<String, Boolean> mColumnsExist;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forkcalllog);
//        Slide slide = (Slide) TransitionInflater.from(this).inflateTransition(R.transition.activity_slide);
//        getWindow().setExitTransition(slide);

        mPresenter = new ForkCallLogPresenter(this);
        if (mPresenter.checkPermissions(this)) {
            initUI();
        }
    }

    private void initUI() {
        mColumnsExist = mPresenter.getColumnsExist(getApplicationContext());

        mPhoneNumberEditText = (EditText) findViewById(R.id.call_log_number_edtxt);
        mStartForkButton = (Button) findViewById(R.id.start_fork_calllog_btn);
        mStartForkButton.setOnClickListener(mOnClickListener);

        mCallLogTypeGroup = (RadioGroup) findViewById(R.id.call_log_type_radioGroup);
        mOutgoingRadioButton = (RadioButton) findViewById(R.id.outgoing_radiobtn);
        mOutgoingRadioButton.setChecked(true);

        mRejectedRadioButton = (RadioButton) findViewById(R.id.rejected_radiobtn);
        mIncomingRadioButton = (RadioButton) findViewById(R.id.answered_radiobtn);
        mMissedRadioButton = (RadioButton) findViewById(R.id.missed_radiobtn);

        mCallLogVolteGroup = (RadioGroup) findViewById(R.id.call_log_volte_feature_radiogroup);
        if (mColumnsExist.get(DataBaseOperator.CALLLOG_CALL_TYPE)) {
            mCallLogVolteGroup.setVisibility(View.VISIBLE);
            mVolteRadioButton = (RadioButton) findViewById(R.id.call_log_volte_radiobtn);
            mVolteRadioButton.setChecked(true);
            mVowifiRadioButton = (RadioButton) findViewById(R.id.call_log_vowifi_hd_radiobtn);
            mHdRadioButton = (RadioButton) findViewById(R.id.call_log_volte_hd_radiobtn);
            mNoneRadioButton = (RadioButton) findViewById(R.id.call_log_volte_none_radiobtn);
        } else {
            mCallLogVolteGroup.setVisibility(View.GONE);
        }

        mEncryptedCallCheckBox = (CheckBox) findViewById(R.id.encrypted_call_chkbox);
        if (mColumnsExist.get(DataBaseOperator.CALLLOG_ENCRYPT)) {
            mEncryptedCallCheckBox.setVisibility(View.VISIBLE);
            mEncryptedCallCheckBox.setChecked(true);
        } else {
            mEncryptedCallCheckBox.setVisibility(View.GONE);
        }

        mVideoCallCheckBox = (CheckBox) findViewById(R.id.video_call_chkbox);
        if (mColumnsExist.get(DataBaseOperator.CALLLOG_FEATURE)) {
            mVideoCallCheckBox.setVisibility(View.VISIBLE);
        } else {
            mVideoCallCheckBox.setVisibility(View.GONE);
        }

        mRollDiceCheckBox = (CheckBox) findViewById(R.id.call_log_roll_dice);
        mRollDiceCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);
        mCallLogQuantityEditText = (EditText) findViewById(R.id.call_log_quantity_edtxt);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    View.OnClickListener mOnClickListener = v -> {
        switch (v.getId()) {
            case R.id.start_fork_calllog_btn:
                startForkCallLogs();
                break;

            default:
                break;
        }
    };

    private void startForkCallLogs() {
        if (!mRollDiceCheckBox.isChecked()) {
            String quantity = mCallLogQuantityEditText.getText().toString();
            if (TextUtils.isEmpty(quantity) || Integer.valueOf(quantity) <= 0) {
                Snackbar.make(findViewById(R.id.activity_fork_call_log_layout), R.string.call_log_quantity_msg, Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                mPresenter.forkSpecifiedCallLog(getApplicationContext(), getKeyValuesToFork());
            }
        } else {
            mPresenter.forkRandomCallLogs(getApplicationContext(), Integer.parseInt(mCallLogQuantityEditText.getText().toString()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.call_log_rcs:

                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    ForkCallLogData getKeyValuesToFork() {
        ForkCallLogData data = new ForkCallLogData();

        data.setPhoneNum(mPhoneNumberEditText.getText().toString());

        int type = CallLog.Calls.INCOMING_TYPE;
        if (mCallLogTypeGroup.getVisibility() == View.VISIBLE) {
            if (mOutgoingRadioButton.isChecked()) {
                type = CallLog.Calls.OUTGOING_TYPE;
            } else if (mRejectedRadioButton.isChecked()) {
                type = CallLog.Calls.REJECTED_TYPE;
            } else if (mIncomingRadioButton.isChecked()) {
                type = CallLog.Calls.INCOMING_TYPE;
            } else {
                type = CallLog.Calls.MISSED_TYPE;
            }
        }
        data.setType(type);

        int volte_type = 0;
        if (mCallLogVolteGroup.getVisibility() == View.VISIBLE) {
            if (mVolteRadioButton.isChecked()) {
                volte_type = 82;
            } else if (mVowifiRadioButton.isChecked()) {
                volte_type = 83;
            } else if (mHdRadioButton.isChecked()) {
                volte_type = 81;
            } else if (mNoneRadioButton.isChecked()) {
                volte_type = 0;
            }
        }
        data.setCallType(volte_type);

        int encrypt_call = 0;
        if (mEncryptedCallCheckBox.getVisibility() == View.VISIBLE
                && mEncryptedCallCheckBox.isChecked()) {
            encrypt_call = 1;
        }
        data.setEnryptCall(encrypt_call);

        int features = 0;
        if (mVideoCallCheckBox.getVisibility() == View.VISIBLE
                && mVideoCallCheckBox.isChecked()) {
            features = 1;
        }
        data.setFeatures(features);

        data.setQuantity(Integer.parseInt(mCallLogQuantityEditText.getText().toString()));
        return data;
    }

    CompoundButton.OnCheckedChangeListener mCheckedChangeListener = (buttonView, isChecked) -> {
        switch (buttonView.getId()) {
            case R.id.call_log_roll_dice:
                updateButtonsStates();
                break;
            default:
                break;
        }
    };

    private void updateButtonsStates() {
        if (mRollDiceCheckBox.isChecked()) {
            setButtonsEnabled(false);
        } else {
            setButtonsEnabled(true);
        }
    }

    private void setButtonsEnabled(boolean isEnabled) {
        mPhoneNumberEditText.setEnabled(isEnabled);
        // call log types
        int calllogTypeSize = mCallLogTypeGroup.getChildCount();
        for (int i = 0; i < calllogTypeSize; ++i) {
            mCallLogTypeGroup.getChildAt(i).setEnabled(isEnabled);
        }

        // call log volte types
        int calllogVolteTypeSize = mCallLogVolteGroup.getChildCount();
        for (int i = 0; i < calllogVolteTypeSize; ++i) {
            mCallLogVolteGroup.getChildAt(i).setEnabled(isEnabled);
        }

        mEncryptedCallCheckBox.setEnabled(isEnabled);
        mVideoCallCheckBox.setEnabled(isEnabled);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
