package com.pioneer.aaron.dolly;

import android.app.Activity;

import com.pioneer.aaron.dolly.utils.PermissionChecker;

/**
 * Created by Aaron on 4/18/17.
 */

public class MainPresenter implements IMainContract.Presenter {

    public MainPresenter() {
    }

    @Override
    public boolean checkPermissions(Activity activity) {
        return PermissionChecker.checkPermissions(activity);
    }
}
