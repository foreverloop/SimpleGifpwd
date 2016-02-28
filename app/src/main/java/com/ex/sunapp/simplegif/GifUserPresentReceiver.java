package com.ex.sunapp.simplegif;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class GifUserPresentReceiver extends BroadcastReceiver {

    BroadcastReceiver screenoffReceiver;
    BroadcastReceiver sleepFilter;
    IntentFilter filter;
    boolean mIsRegistered;

    @Override
    public void onReceive(Context context, Intent intent) {

        if(filter == null){
            filter = new IntentFilter();
        }

        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        if (intent.getAction() != null) {
            if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {

                if(!SimpleGifDecodeService.sRunning){
                    startGifService1(context);

                }
            }
        }

        screenoffReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {


                if (intent.getAction() != null) {
                    if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

                        //1
                        Intent in = new Intent(context, SimpleGifDecodeService.class);
                        context.stopService(in);

                        IntentFilter onFilter = new IntentFilter();
                        onFilter.addAction(Intent.ACTION_SCREEN_ON);

                        sleepFilter = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                context.unregisterReceiver(this);
                                if(!SimpleGifDecodeService.sRunning)
                                    startGifService1(context);
                            }
                        };

                        context.getApplicationContext().registerReceiver(sleepFilter, onFilter);
                        cleanUpReceivers(context);
                    }
                }
            }
        };

        context.getApplicationContext().registerReceiver(screenoffReceiver, filter);
        mIsRegistered = true;
    }

    private void startGifService1(Context context){

        int[] wids;
        AppWidgetManager am = AppWidgetManager.getInstance(context);
        wids = am.getAppWidgetIds(new ComponentName(context, SimpleGifWidgetProvider.class));

        if(wids.length < 1)
            return;

        Intent in2 = new Intent(context, SimpleGifDecodeService.class);
        context.startService(in2);

    }

    public void cleanUpReceivers(Context context){

        if(mIsRegistered) {
            context.getApplicationContext().unregisterReceiver(screenoffReceiver);
            screenoffReceiver = null;
        }
    }
}
