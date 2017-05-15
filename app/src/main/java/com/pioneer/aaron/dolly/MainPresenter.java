package com.pioneer.aaron.dolly;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.provider.CallLog;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pioneer.aaron.dolly.fork.ForkService;
import com.pioneer.aaron.dolly.fork.ForkTask;
import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogData;
import com.pioneer.aaron.dolly.utils.PermissionChecker;

/**
 * Created by Aaron on 4/18/17.
 */

public class MainPresenter implements IMainContract.Presenter {

    private static final int CALLLOG_DEFAULT_QUANTITY = 5;
    private static final int VIBRATE_ON_LONG_CLICK = 100;
    private ForkService.ForkBinder mForkBinder;
    private Context mContext;
    private MainHanler mHanler;
    private class MainHanler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VIBRATE_ON_LONG_CLICK:
                    Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(70);
                    break;
                default:
                    break;
            }
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mForkBinder = (ForkService.ForkBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public MainPresenter(Context context) {
        mContext = context;
        Intent intent = new Intent(context, ForkService.class);
        context.startService(intent);
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        mHanler = new MainHanler();
    }

    @Override
    public boolean checkPermissions(Activity activity) {
        return PermissionChecker.checkPermissions(activity);
    }

    @Override
    public void forkRCS(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.fork_rcs_calllog_layout, null);
        final EditText numberEditText = (EditText) view.findViewById(R.id.rcs_call_log_number);
        final EditText subjectEditText = (EditText) view.findViewById(R.id.rcs_call_log_subject);
        final EditText postCallEditText = (EditText) view.findViewById(R.id.rcs_call_log_post_call_text);
        final EditText quantityEditText = (EditText) view.findViewById(R.id.rcs_call_log_quantity);
        final RadioGroup typeRadioGroup = (RadioGroup) view.findViewById(R.id.call_log_type_radioGroup);
        final int type = getTypeChecked(view);


        quantityEditText.setText(String.valueOf(CALLLOG_DEFAULT_QUANTITY));
        final CheckBox rollDiceCheckBox = (CheckBox) view.findViewById(R.id.call_log_roll_dice);
        rollDiceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean enabled = !isChecked;
                numberEditText.setEnabled(enabled);
                subjectEditText.setEnabled(enabled);
                postCallEditText.setEnabled(enabled);
                int childSize = typeRadioGroup.getChildCount();
                for (int i =0;i<childSize;++i) {
                    RadioButton child = (RadioButton) typeRadioGroup.getChildAt(i);
                    child.setEnabled(enabled);
                }
            }
        });

        builder.setTitle(R.string.menu_call_log_rcs)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.start_fork_call_logs, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (rollDiceCheckBox.isChecked()) {
                            startForkRandomRCS(Integer.parseInt(quantityEditText.getText().toString()));
                        } else {
                            ForkCallLogData data = new ForkCallLogData();
                            data.setPhoneNum(numberEditText.getText().toString());
                            data.setType(type);
                            data.setSubject(subjectEditText.getText().toString());
                            data.setPostCallText(postCallEditText.getText().toString());
                            data.setQuantity(Integer.parseInt(quantityEditText.getText().toString()));
                            startForkSpecifiedRCS(data);
                        }
                    }
                })
                .setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private int getTypeChecked(View view) {
        RadioButton outgoingRadioButton = (RadioButton) view.findViewById(R.id.outgoing_radiobtn);
        outgoingRadioButton.setChecked(true);
        RadioButton rejectedRadioButton = (RadioButton) view.findViewById(R.id.rejected_radiobtn);
        RadioButton incomingRadioButton = (RadioButton) view.findViewById(R.id.rejected_radiobtn);
        RadioButton missedRadioButton = (RadioButton) view.findViewById(R.id.rejected_radiobtn);
        int type;
        if (outgoingRadioButton.isChecked()) {
            type = CallLog.Calls.OUTGOING_TYPE;
        } else if (rejectedRadioButton.isChecked()) {
            type = CallLog.Calls.REJECTED_TYPE;
        } else if (incomingRadioButton.isChecked()) {
            type = CallLog.Calls.INCOMING_TYPE;
        } else {
            type = CallLog.Calls.MISSED_TYPE;
        }
        return type;
    }

    @Override
    public void onDestroy(Context context) {
        context.unbindService(mServiceConnection);
    }

    @Override
    public void vibrate() {
        mHanler.sendEmptyMessage(VIBRATE_ON_LONG_CLICK);
    }

    private void startForkRandomRCS(int quantity) {
        if (mForkBinder != null) {
            mForkBinder.startFork(ForkTask.FORK_TYPE_RANDOM_RCS_CALLLOGS, quantity);
        }
    }

    private void startForkSpecifiedRCS(ForkCallLogData data) {
        if (mForkBinder != null) {
            mForkBinder.startFork(ForkTask.FORK_TYPE_SPECIFIED_RCS_CALLLOGS, data.getQuantity(), data);
        }
    }
}
