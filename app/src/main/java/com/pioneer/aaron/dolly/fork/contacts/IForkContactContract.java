package com.pioneer.aaron.dolly.fork.contacts;

import android.content.Context;

import com.pioneer.aaron.dolly.IBasePresenter;
import com.pioneer.aaron.dolly.IBaseView;

/**
 * Created by Aaron on 4/30/17.
 */

public interface IForkContactContract {

    interface View extends IBaseView {

    }

    interface Presenter extends IBasePresenter {
        void forkContacts(Context context, int quantity, boolean allTypes);

        void onDestroy(Context context);
    }

}
