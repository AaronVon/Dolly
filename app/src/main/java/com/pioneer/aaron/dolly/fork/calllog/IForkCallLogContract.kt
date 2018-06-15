package com.pioneer.aaron.dolly.fork.calllog

import android.content.Context
import com.pioneer.aaron.dolly.IBasePresenter
import com.pioneer.aaron.dolly.IBaseView
import java.util.*

/**
 * Created by Aaron on 4/28/17.
 */

interface IForkCallLogContract {

    interface View : IBaseView {
        fun toast(msg: String)
    }

    interface Presenter : IBasePresenter {
        fun getColumnsExist(context: Context): HashMap<String, Boolean>

        fun forkRandomCallLogs(context: Context, quantity: Int)

        fun forkSpecifiedCallLog(context: Context, data: ForkCallLogData)

        fun forkVvmCallLog(context: Context, phoneNumber: String, subId: Int)

        fun sendVvmState(vvmState: String, state: String)

        fun vibrate()

        fun toast(msg: String)
    }

}
