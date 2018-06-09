package com.pioneer.aaron.dolly

import android.content.Context

/**
 * Created by Aaron on 4/18/17.
 */

interface IMainContract {

    interface View : IBaseView

    interface Presenter : IBasePresenter {
        fun forkRCS(context: Context)

        override fun onDestroy(context: Context)

        fun vibrate()
    }

}
