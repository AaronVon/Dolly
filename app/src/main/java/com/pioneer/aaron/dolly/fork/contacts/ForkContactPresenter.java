package com.pioneer.aaron.dolly.fork.contacts;

import android.app.Activity;
import android.content.Context;

import com.pioneer.aaron.dolly.fork.DataBaseOpearator;
import com.pioneer.aaron.dolly.utils.PermissionChecker;

/**
 * Created by Aaron on 4/30/17.
 */

public class ForkContactPresenter implements IForkContactContract.Presenter {

    @Override
    public boolean checkPermissions(Activity activity) {
        return PermissionChecker.checkPermissions(activity);
    }

    @Override
    public void forkContacts(Context context, int quantity) {
        DataBaseOpearator.getInstance(context).forkContacts(quantity);
    }
}
