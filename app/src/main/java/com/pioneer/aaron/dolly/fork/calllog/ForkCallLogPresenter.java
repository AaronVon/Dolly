package com.pioneer.aaron.dolly.fork.calllog;

import android.app.Activity;
import android.content.Context;

import com.pioneer.aaron.dolly.fork.DataBaseOpearator;
import com.pioneer.aaron.dolly.utils.PermissionChecker;

import java.util.HashMap;

/**
 * Created by Aaron on 4/28/17.
 */

public class ForkCallLogPresenter implements IForkCallLogContract.Presenter {

    @Override
    public HashMap<String, Boolean> checkIfColumnsExist(Context context, String... columns) {
        return DataBaseOpearator.getInstance(context).checkColumnsExists(columns);
    }

    @Override
    public void forkRandomCallLogs(Context context, int quantity) {
        DataBaseOpearator.getInstance(context).forkRandomCallLogs(context, quantity);
    }

    @Override
    public void forkSpecifiedCallLog(Context context, ForkCallLogData data) {
        DataBaseOpearator.getInstance(context).forkSpecifiedCallLog(context, data);
    }


    @Override
    public boolean checkPermissions(Activity activity) {
        return PermissionChecker.checkPermissions(activity);
    }
}
