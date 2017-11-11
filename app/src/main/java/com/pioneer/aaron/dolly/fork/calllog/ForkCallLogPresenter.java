package com.pioneer.aaron.dolly.fork.calllog;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.VoicemailContract;
import android.util.Log;

import com.pioneer.aaron.dolly.R;
import com.pioneer.aaron.dolly.fork.DataBaseOperator;
import com.pioneer.aaron.dolly.fork.ForkService;
import com.pioneer.aaron.dolly.fork.ForkTask;
import com.pioneer.aaron.dolly.utils.ForkConstants;
import com.pioneer.aaron.dolly.utils.ForkVibrator;
import com.pioneer.aaron.dolly.utils.Matrix;
import com.pioneer.aaron.dolly.utils.PermissionChecker;

import java.util.HashMap;

/**
 * Created by Aaron on 4/28/17.
 */

public class ForkCallLogPresenter implements IForkCallLogContract.Presenter {
    private static final String TAG = "ForkCallLogPresenter";
    private MyHandler mHandler;
    private Context mContext;
    private ForkService.ForkBinder mForkBinder;
    private IForkCallLogContract.View mView;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mForkBinder = (ForkService.ForkBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public ForkCallLogPresenter(Context context, IForkCallLogContract.View view) {
        mView = view;
        Intent intent = new Intent(context, ForkService.class);
        mContext = context;
        context.startService(intent);
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        mHandler = new MyHandler();
    }

    private class MyHandler extends Handler {
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

    @Override
    public HashMap<String, Boolean> getColumnsExist(Context context) {
        return DataBaseOperator.getInstance(context).getColumnsExists();
    }

    @Override
    public void forkRandomCallLogs(Context context, int quantity) {
        if (mForkBinder != null) {
            mForkBinder.startFork(ForkTask.FORK_TYPE_RANDOM_CALLLOGS, quantity);
        }
    }

    @Override
    public void forkSpecifiedCallLog(Context context, ForkCallLogData data) {
        if (mForkBinder != null) {
            mForkBinder.startFork(ForkTask.FORK_TYPE_SPECIFIED_CALLLOGS, data.getQuantity(), data);
        }
    }

    @Override
    public void forkVvmCallLog(Context context, String phoneNumber) {
        if (mForkBinder != null) {
            mForkBinder.startFork(ForkTask.FORK_TYPE_VVM, phoneNumber);
        }
    }

    @Override
    public void sendVvmState(String state) {
        new ConfigStateAsyncTask().execute(state);
    }

    private class ConfigStateAsyncTask extends AsyncTask<String, Void, Void> {
        private int mConfigState = ForkConstants.INVALID_CONFIG_STATE;
        private String state;

        @Override
        protected Void doInBackground(String... params) {
            String[] configs = mContext.getResources().getStringArray(R.array.vvm_config_states);
            state = params[0];
            for (int i = 0; i < configs.length; ++i) {
                if (configs[i].equals(state)) {
                    mConfigState = i;
                    break;
                }
            }
            ContentResolver contentResolver = mContext.getContentResolver();
            Uri configUri = VoicemailContract.Status.buildSourceUri(mContext.getPackageName());

            ContentValues contentValues = new ContentValues();
            contentValues.put(VoicemailContract.Status.CONFIGURATION_STATE, mConfigState);
            contentValues.put(VoicemailContract.Status.NOTIFICATION_CHANNEL_STATE, VoicemailContract.Status.NOTIFICATION_CHANNEL_STATE_NO_CONNECTION);
            contentValues.put(VoicemailContract.Status.VOICEMAIL_ACCESS_URI, "tel:888");
            contentValues.put(VoicemailContract.Status.DATA_CHANNEL_STATE, VoicemailContract.Status.DATA_CHANNEL_STATE_NO_CONNECTION);
            contentValues.put(VoicemailContract.Status.SETTINGS_URI, Uri.decode("http://www.default.config.com"));
            contentValues.put(VoicemailContract.Status.SOURCE_PACKAGE, mContext.getPackageName());

            contentResolver.insert(configUri, contentValues);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mConfigState != ForkConstants.INVALID_CONFIG_STATE) {
                Log.i(TAG, "state " + mConfigState + " was send.");
                mView.toast("State " + state + " was send.");
            }
        }
    }

    @Override
    public void vibrate() {
        mHandler.sendEmptyMessage(ForkConstants.VIBRATE_ON_LONG_CLICK);
    }

    @Override
    public boolean checkPermissions(Activity activity) {
        return PermissionChecker.checkPermissions(activity);
    }

    @Override
    public void loadResInBackground(Context context) {
        // load existing contact phone number
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                Matrix.preloadContactPhoneNums(context);
                return null;
            }
        };
        asyncTask.execute();
    }

    @Override
    public void onDestroy(Context context) {
        try {
            context.unbindService(mServiceConnection);
        } catch (IllegalStateException ise) {
            Log.e(TAG, "onDestroy: unbindService IllegalStateException");
        }
    }
}
