package com.pioneer.aaron.dolly.fork.calllog;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import com.pioneer.aaron.dolly.R;
import com.pioneer.aaron.dolly.fork.DataBaseOperator;
import com.pioneer.aaron.dolly.fork.ForkService;
import com.pioneer.aaron.dolly.fork.ForkTask;
import com.pioneer.aaron.dolly.utils.PermissionChecker;

import java.util.HashMap;

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
            toastForkTask(context);
        }
    }

    @Override
    public void forkSpecifiedCallLog(Context context, ForkCallLogData data) {
        if (mForkBinder != null) {
            mForkBinder.startFork(ForkTask.FORK_TYPE_SPECIFIED_CALLLOGS, data.getQuantity(), data);
            toastForkTask(context);
        }
    }


    @Override
    public boolean checkPermissions(Activity activity) {
        return PermissionChecker.checkPermissions(activity);
    }

    @Override
    public void toastForkTask(Context context) {
        Toast.makeText(context, context.getResources().getString(R.string.fork_task_forking), Toast.LENGTH_SHORT).show();
    }
}
