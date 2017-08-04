package com.pioneer.aaron.dolly.fork.contacts;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.pioneer.aaron.dolly.R;
import com.pioneer.aaron.dolly.fork.ForkService;
import com.pioneer.aaron.dolly.fork.ForkTask;
import com.pioneer.aaron.dolly.utils.PermissionChecker;

/**
 * Created by Aaron on 4/30/17.
 */

public class ForkContactPresenter implements IForkContactContract.Presenter {

    private ForkService.ForkBinder mForkBinder;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("Aaron", "onServiceConnected: ");
            mForkBinder = (ForkService.ForkBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("Aaron", "onServiceDisconnected: ");
        }
    };

    public ForkContactPresenter(Context context) {
        Intent intent = new Intent(context, ForkService.class);
        context.startService(intent);
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean checkPermissions(Activity activity) {
        return PermissionChecker.checkPermissions(activity);
    }

    @Override
    public void loadResInBackground() {

    }

    @Override
    public void toastForkTask(Context context) {
        Toast.makeText(context, context.getResources().getString(R.string.fork_task_forking), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void forkContacts(Context context, int quantity, boolean allTypes) {
        if (mForkBinder != null) {
            mForkBinder.startFork(allTypes ? ForkTask.FORK_TYPE_ALL_TYPE_CONTACT : ForkTask.FORK_TYPE_RANDOM_CONTACT,
                    quantity);
            toastForkTask(context);
        }
    }

    @Override
    public void onDestroy(Context context) {
        context.unbindService(mServiceConnection);
    }
}
