package com.pioneer.aaron.dolly.fork;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.pioneer.aaron.dolly.MainActivity;
import com.pioneer.aaron.dolly.R;
import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogData;

/**
 * Created by Aaron on 5/1/17.
 */

public class ForkService extends Service {

    private ForkTask mForkTask;
    private int total;

    private IForkListener mForkListener = new IForkListener() {
        @Override
        public void onProgress(int percentProgress, int realProgress) {
            getNotificationManager()
                    .notify(1,
                            getNotification(getResources().getString(R.string.fork_task_forking),
                                    percentProgress, realProgress)
                    );
        }

        @Override
        public void onCompleted() {
            mForkTask = null;
            stopForeground(true);
            getNotificationManager()
                    .notify(1, getNotification(getResources().getString(R.string.fork_task_completed), -1, -1));
        }

        @Override
        public void onCanceled() {
            mForkTask = null;
            stopForeground(true);
            Toast.makeText(ForkService.this,
                    getResources().getString(R.string.fork_task_canceled), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            mForkTask = null;
            stopForeground(true);
            getNotificationManager()
                    .notify(1, getNotification(getResources().getString(R.string.fork_task_failed), -1, -1));
            Toast.makeText(ForkService.this, getResources().getString(R.string.fork_task_failed), Toast.LENGTH_SHORT).show();
        }
    };

    private Binder mBinder = new ForkBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ForkBinder extends Binder {
        public void startFork(int fork_type, int forkQuantity) {
            startFork(fork_type, forkQuantity, null);
        }

        public void startFork(int fork_type, int forkQuantity, ForkCallLogData data) {
            if (mForkTask == null) {
                switch (fork_type) {
                    case ForkTask.FORK_TYPE_SPECIFIED_CALLLOGS:
                        mForkTask = new ForkTask(mForkListener, getApplicationContext());
                        mForkTask.execute(fork_type, forkQuantity, data);
                        Toast.makeText(ForkService.this,
                                getResources().getString(R.string.fork_task_forking),
                                Toast.LENGTH_SHORT).show();
                        total = forkQuantity;
                        break;
                    case ForkTask.FORK_TYPE_RANDOM_CALLLOGS:
                        mForkTask = new ForkTask(mForkListener, getApplicationContext());
                        mForkTask.execute(fork_type, forkQuantity);
                        Toast.makeText(ForkService.this,
                                getResources().getString(R.string.fork_task_forking),
                                Toast.LENGTH_SHORT).show();
                        total = forkQuantity;
                        break;
                    case ForkTask.FORK_TYPE_RANDOM_CONTACT:
                        mForkTask = new ForkTask(mForkListener, getApplicationContext());
                        mForkTask.execute(fork_type, forkQuantity);
                        Toast.makeText(ForkService.this,
                                getResources().getString(R.string.fork_task_forking),
                                Toast.LENGTH_SHORT).show();
                        total = forkQuantity;
                        break;
                    default:
                        break;
                }
            } else {
                Toast.makeText(ForkService.this,
                        getResources().getString(R.string.fork_already_in_procedure_msg),
                        Toast.LENGTH_SHORT)
                        .show();
            }

        }

        public void cancelFork() {
            if (mForkTask != null) {
                mForkTask.cancelFork();
            }
        }
    }


    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress, int realProgress) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        if (progress > 0) {
            builder.setContentText(realProgress + "/" + total);
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }
}
