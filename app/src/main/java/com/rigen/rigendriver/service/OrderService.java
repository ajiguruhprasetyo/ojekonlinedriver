package com.rigen.rigendriver.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.rigen.rigendriver.DetailOrderActivity;
import com.rigen.rigendriver.R;

/**
 * Created by ILHAM HP on 20/11/2016.
 */

public class OrderService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        Toast.makeText(this, "Start",Toast.LENGTH_SHORT).show();
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "Msg",Toast.LENGTH_SHORT).show();
        Intent resultIntent = new Intent(this, DetailOrderActivity.class);
        resultIntent.putExtra("id", intent.getIntExtra("id",1)+"");
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        android.support.v7.app.NotificationCompat.Builder mNotifyBuilder;
        NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Sets an ID for the notification, so it can be updated
        mNotifyBuilder = new android.support.v7.app.NotificationCompat.Builder(this);
        mNotifyBuilder.setContentTitle("Order Baru");
        mNotifyBuilder.setContentText(intent.getStringExtra("alamat"));
        mNotifyBuilder.setSmallIcon(R.drawable.ic_map_drop);
        // Set pending intent
        mNotifyBuilder.setContentIntent(resultPendingIntent);
        // Set Vibrate, Sound and Light
        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;
        mNotifyBuilder.setDefaults(defaults);
        // Set the content for Notification
        mNotifyBuilder.setContentInfo(intent.getDoubleExtra("jarak", 0)+" Km");
        mNotifyBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_map_drop));
        // Set autocancel
        mNotifyBuilder.setAutoCancel(true);
        mNotificationManager.notify( intent.getIntExtra("id",1), mNotifyBuilder.build());
    }
}
