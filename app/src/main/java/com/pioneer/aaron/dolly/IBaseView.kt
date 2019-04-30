package com.pioneer.aaron.dolly

import android.app.Activity
import com.pioneer.aaron.dolly.utils.checkPermissions

/**
 * Created by Aaron on 4/29/17.
 */

interface IBaseView {
    fun checkPermission(activity: Activity): Boolean = checkPermissions(activity)
}
