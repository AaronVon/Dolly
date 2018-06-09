package com.pioneer.aaron.dolly.fork.contacts

import android.content.Context

import com.pioneer.aaron.dolly.IBasePresenter
import com.pioneer.aaron.dolly.IBaseView

/**
 * Created by Aaron on 4/30/17.
 */

interface IForkContactContract {

    interface View : IBaseView

    interface Presenter : IBasePresenter {
        fun forkContacts(context: Context, quantity: Int, allTypes: Boolean, avatarIncluded: Boolean)

        override fun onDestroy(context: Context)
    }

}
