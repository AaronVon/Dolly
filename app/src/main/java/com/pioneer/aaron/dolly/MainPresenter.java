package com.pioneer.aaron.dolly;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.provider.CallLog;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pioneer.aaron.dolly.fork.ForkService;
import com.pioneer.aaron.dolly.fork.ForkTask;
import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogData;
import com.pioneer.aaron.dolly.utils.ForkConstants;
import com.pioneer.aaron.dolly.utils.ForkVibrator;
import com.pioneer.aaron.dolly.utils.Matrix;
import com.pioneer.aaron.dolly.utils.PermissionChecker;

/**
 * Created by Aaron on 4/18/17.
 */

public class MainPresenter implements IMainContract.Presenter {
    private static final String TAG = "MainPresenter";

    private static final int CALLLOG_DEFAULT_QUANTITY = 5;
    private ForkService.ForkBinder mForkBinder;
    private Context mContext;
    private MainHanler mHanler;
    private class MainHanler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ForkConstants.VIBRATE_ON_LONG_CLICK:
                    ForkVibrator.getInstance(mContext).vibrate(70);
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
    public void loadResInBackground(Context context) {
        AsyncTask<Void,Void,Void> loadResAsyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Matrix.loadResources(mContext, true);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d(TAG, "onPostExecute: load res finished in background");
            }
        };
        loadResAsyncTask.execute();
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
        rollDiceCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean enabled = !isChecked;
            numberEditText.setEnabled(enabled);
            subjectEditText.setEnabled(enabled);
            postCallEditText.setEnabled(enabled);
            int childSize = typeRadioGroup.getChildCount();
            for (int i =0;i<childSize;++i) {
                RadioButton child = (RadioButton) typeRadioGroup.getChildAt(i);
                child.setEnabled(enabled);
            }
        });

        builder.setTitle(R.string.menu_call_log_rcs)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.start_fork_call_logs, (dialog, which) -> {
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
                })
                .setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private int getTypeChecked(View view) {
        RadioButton outgoingRadioButton = (RadioButton) view.findViewById(R.id.outgoing_radiobtn);
        outgoingRadioButton.setChecked(true);
        RadioButton rejectedRadioButton = (RadioButton) view.findViewById(R.id.rejected_radiobtn);
        RadioButton incomingRadioButton = (RadioButton) view.findViewById(R.id.incoming_radiobtn);
//        RadioButton missedRadioButton = (RadioButton) view.findViewById(R.id.missed_radiobtn);
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
        mHanler.sendEmptyMessage(ForkConstants.VIBRATE_ON_LONG_CLICK);
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
