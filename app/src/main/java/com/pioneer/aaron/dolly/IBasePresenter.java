package com.pioneer.aaron.dolly;

import android.app.Activity;
import android.content.Context;

/**
 * Created by Aaron on 4/29/17.
 */

public interface IBasePresenter {
    boolean checkPermissions(Activity activity);

    void loadResInBackground();

    void toastForkTask(Context context);
}
