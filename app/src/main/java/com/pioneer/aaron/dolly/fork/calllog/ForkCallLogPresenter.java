package com.pioneer.aaron.dolly.fork.calllog;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.pioneer.aaron.dolly.fork.DataBaseOperator;
import com.pioneer.aaron.dolly.fork.ForkService;
import com.pioneer.aaron.dolly.fork.ForkTask;
import com.pioneer.aaron.dolly.utils.Matrix;
import com.pioneer.aaron.dolly.utils.PermissionChecker;

import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by Aaron on 4/28/17.
 */

public class ForkCallLogPresenter implements IForkCallLogContract.Presenter {

    private ForkService.ForkBinder mForkBinder;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mForkBinder = (ForkService.ForkBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public ForkCallLogPresenter(Context context) {
        Intent intent = new Intent(context, ForkService.class);
        context.startService(intent);
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
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
