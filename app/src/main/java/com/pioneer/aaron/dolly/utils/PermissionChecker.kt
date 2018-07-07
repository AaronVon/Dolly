package com.pioneer.aaron.dolly.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

/**
 * Created by Aaron on 4/29/17.
 */

object PermissionChecker {

    const val PERMISSION_REQUEST_CODE = 100

    private val PERMISSIONS_NEEDED = arrayOf(Manifest.permission.ADD_VOICEMAIL, Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)

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
}
