package com.pioneer.aaron.dolly.fork.contacts

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

import com.pioneer.aaron.dolly.fork.ForkService
import com.pioneer.aaron.dolly.fork.ForkTask
import com.pioneer.aaron.dolly.utils.PermissionChecker

/**
 * Created by Aaron on 4/30/17.
 */

class ForkContactPresenter(context: Context) : IForkContactContract.Presenter {

    private lateinit var mForkBinder: ForkService.ForkBinder
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected: ")
            mForkBinder = service as ForkService.ForkBinder
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "onServiceDisconnected: ")
        }
    }

    init {
        val intent = Intent(context, ForkService::class.java)
        context.startService(intent)
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun checkPermissions(activity: Activity): Boolean {
        return PermissionChecker.checkPermissions(activity)
    }

    override fun loadResInBackground(context: Context) {

    }

    override fun forkContacts(context: Context, quantity: Int, allTypes: Boolean, avatarIncluded: Boolean) {
        mForkBinder.startFork(if (allTypes) ForkTask.FORK_TYPE_ALL_TYPE_CONTACT else ForkTask.FORK_TYPE_RANDOM_CONTACT,
                quantity, avatarIncluded)
    }

    override fun onDestroy(context: Context) {
        try {
            context.unbindService(mServiceConnection)
        } catch (ise: IllegalStateException) {
            Log.e(TAG, "onDestroy: unbindService IllegalStateException")
        }

    }

    companion object {
        private const val TAG = "ForkContactPresenter"
    }
}
