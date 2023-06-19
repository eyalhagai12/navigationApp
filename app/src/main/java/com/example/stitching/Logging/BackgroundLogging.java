package com.example.stitching.Logging;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.stitching.R;
import com.example.stitching.activities.MainActivity;

public class BackgroundLogging extends Service {

    private static final int NOTIF_ID = 23453;
    private static final String NOTIF_CHANNEL_ID = "stitching_log_id";
    private Handler handler;
    private Runnable updateLogTask;
    NotificationManager notificationManager;
    private static final int LOG_INTERVAL = 500;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        NotificationChannel channel = null;
        // should work on >=oreo
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Log.i("backgroundLog","creating notification channel");
            channel = new NotificationChannel(NOTIF_CHANNEL_ID, "stitching notification channel", NotificationManager.IMPORTANCE_DEFAULT);

            channel.setDescription("stitching channel");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

        }
        // do your jobs here
        Log.i("BackgroundLog", "starting");
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                System.out.println("myHandler: here!"); // Do your work here
                handler.postDelayed(this, LOG_INTERVAL);
            }
        }, LOG_INTERVAL);

        startForeground();


        return super.onStartCommand(intent, flags, startId);
    }

    private void startForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);


        Notification notification = new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID).setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Service is running background")
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIF_ID, notification);




    }
}
