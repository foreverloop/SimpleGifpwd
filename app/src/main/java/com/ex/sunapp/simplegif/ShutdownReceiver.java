package com.ex.sunapp.simplegif;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ShutdownReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent in = new Intent(context, SimpleGifDecodeService.class);
        context.stopService(in);

    }
}