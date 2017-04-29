package com.pioneer.aaron.dolly.fork.calllog;

import android.content.Context;

import com.pioneer.aaron.dolly.IBasePresenter;
import com.pioneer.aaron.dolly.IBaseView;

import java.util.HashMap;

/**
 * Created by Aaron on 4/28/17.
 */

public interface IForkCallLogContract {

    interface View extends IBaseView{

    }

    interface Presenter extends IBasePresenter {
        HashMap<String, Boolean> checkIfColumnsExist(Context context, String... columns);

        void forkRandomCallLogs(Context context, int quantity);

        void forkSpecifiedCallLog(Context context, ForkCallLogData data);
    }

}
