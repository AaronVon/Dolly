package com.pioneer.aaron.dolly

import android.app.Activity
import android.content.Context

/**
 * Created by Aaron on 4/29/17.
 */

interface IBasePresenter {
    fun checkPermissions(activity: Activity): Boolean

    fun loadResInBackground(context: Context)

    fun onDestroy(context: Context)
}
