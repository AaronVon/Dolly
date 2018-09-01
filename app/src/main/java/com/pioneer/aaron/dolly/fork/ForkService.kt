package com.pioneer.aaron.dolly.fork

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.pioneer.aaron.dolly.MainActivity
import com.pioneer.aaron.dolly.R
import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogData

/**
 * Created by Aaron on 5/1/17.
 */

class ForkService : Service() {

    private var mForkTask: ForkTask? = null
    private var total: Int = 0

    private val mForkListener = object : IForkListener {
        override fun onProgress(percentProgress: Int, realProgress: Int) {
            notificationManager
                    .notify(1,
                            getNotification(resources.getString(R.string.fork_task_forking),
                                    percentProgress, realProgress)
                    )
        }

        override fun onCompleted() {
            mForkTask = null
            stopForeground(true)
            notificationManager
                    .notify(1, getNotification(resources.getString(R.string.fork_task_completed), -1, -1))
        }

        override fun onCanceled() {
            mForkTask = null
            stopForeground(true)
            Toast.makeText(this@ForkService,
                    resources.getString(R.string.fork_task_canceled), Toast.LENGTH_SHORT).show()
        }

        override fun onFailed() {
            mForkTask = null
            stopForeground(true)
            notificationManager
                    .notify(1, getNotification(resources.getString(R.string.fork_task_failed), -1, -1))
            Toast.makeText(this@ForkService, resources.getString(R.string.fork_task_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private val mBinder = ForkBinder()

    private val notificationManager: NotificationManager
        get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    inner class ForkBinder : Binder() {

        fun startFork(fork_type: Int, phoneNumber: String, subId: Int) {
            val data = ForkCallLogData()
            data.phoneNum = phoneNumber
            data.subId = subId
            startFork(fork_type, 0, false, data)
        }

        fun startFork(fork_type: Int, forkQuantity: Int, data: ForkCallLogData) {
            startFork(fork_type, forkQuantity, false, data)
        }

        @JvmOverloads
        fun startFork(fork_type: Int, forkQuantity: Int, avatarIncluded: Boolean = false, data: ForkCallLogData? = null) {
            if (mForkTask == null) {
                mForkTask = ForkTask(mForkListener, applicationContext)
                toastForking(true)
                when (fork_type) {
                    ForkTask.FORK_TYPE_SPECIFIED_CALLLOGS -> {
                        mForkTask!!.execute(fork_type, forkQuantity, data)
                        total = forkQuantity
                    }

                    ForkTask.FORK_TYPE_RANDOM_CALLLOGS -> {
                        mForkTask!!.execute(fork_type, forkQuantity)
                        total = forkQuantity
                    }

                    ForkTask.FORK_TYPE_RANDOM_CONTACT -> {
                        mForkTask!!.execute(fork_type, forkQuantity, avatarIncluded)
                        total = forkQuantity
                    }

                    ForkTask.FORK_TYPE_ALL_TYPE_CONTACT -> {
                        mForkTask!!.execute(fork_type, forkQuantity, avatarIncluded)
                        total = forkQuantity
                    }

                    ForkTask.FORK_TYPE_RANDOM_RCS_CALLLOGS -> {
                        mForkTask!!.execute(fork_type, forkQuantity)
                        total = forkQuantity
                    }
                    ForkTask.FORK_TYPE_SPECIFIED_RCS_CALLLOGS -> {
                        mForkTask!!.execute(fork_type, forkQuantity, data)
                        total = forkQuantity
                    }
                    ForkTask.FORK_TYPE_VVM -> {
                        mForkTask!!.execute(fork_type, data)
                        total = 1
                    }
                    else -> {
                    }
                }
            } else {
                toastForking(false)
            }

        }

        fun cancelFork() {
            mForkTask?.cancelFork()
        }
    }

    private fun toastForking(isForking: Boolean) {
        Toast.makeText(this, resources.getString(if (isForking) R.string.fork_task_forking else R.string.fork_already_in_procedure_msg),
                Toast.LENGTH_SHORT).show()
    }

    private fun getNotification(title: String, progress: Int, realProgress: Int): Notification {
        initNotificationChannel()
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val builder = NotificationCompat.Builder(this, TAG)
        builder.setSmallIcon(R.mipmap.fork_icon)
        builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.fork_icon))
        builder.setContentIntent(pendingIntent)
        builder.setContentTitle(title)
        if (progress > 0) {
            builder.setContentText(realProgress.toString() + "/" + total)
            builder.setProgress(100, progress, false)
        }
        return builder.build()
    }

    private fun initNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val notificationManager = notificationManager
        val notificationChannel = NotificationChannel(TAG, packageName, NotificationManager.IMPORTANCE_DEFAULT)
        notificationChannel.description = TAG
        notificationManager.createNotificationChannel(notificationChannel)
    }

    companion object {
        private val TAG = "ForkService"
    }
}
