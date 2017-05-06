package com.pioneer.aaron.dolly;

import android.content.Context;

/**
 * Created by Aaron on 4/18/17.
 */

public interface IMainContract {

    interface View extends IBaseView {

    }

    interface Presenter extends IBasePresenter {
        void forkRCS(Context context);

        void onDestroy(Context context);
    }

}
