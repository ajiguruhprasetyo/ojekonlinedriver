package com.rigen.rigendriver.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        context.startService(new Intent(context, BookingService.class));
    }
}