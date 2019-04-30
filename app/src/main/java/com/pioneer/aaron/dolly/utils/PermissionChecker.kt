package com.pioneer.aaron.dolly.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.pioneer.aaron.dolly.utils.PermissionChecker.PERMISSIONS_NEEDED
import com.pioneer.aaron.dolly.utils.PermissionChecker.PERMISSION_REQUEST_CODE

/**
 * Created by Aaron on 4/29/17.
 */

object PermissionChecker {
    const val PERMISSION_REQUEST_CODE = 100

    val PERMISSIONS_NEEDED = arrayOf(Manifest.permission.ADD_VOICEMAIL, Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)

}

/**
 * Check permissions requires to execute logic.
 *
 * @param activity
 * @return true if permissions are all granted, or false the otherwise.
 */
fun checkPermissions(activity: Activity): Boolean {
    return if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ADD_VOICEMAIL) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity, PERMISSIONS_NEEDED, PERMISSION_REQUEST_CODE)
        false
    } else {
        true
    }
}

/**
 * Check if [permissions] are all granted or not.
 *
 * @param context
 * @param permissions to check.
 * @return true if all [permissions] are granted.
 */
fun isPermissionsGranted(context: Context, permissions: List<String>): Boolean {
    var granted = true
    for (permission in permissions) {
        granted = granted && (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED)
    }
    return granted
}
