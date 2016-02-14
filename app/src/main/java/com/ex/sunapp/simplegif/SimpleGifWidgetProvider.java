package com.ex.sunapp.simplegif;

import android.app.AlarmManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class SimpleGifWidgetProvider extends AppWidgetProvider {

    public static final String FILE_KEY = "SimpleGifWidKey";
    private static final String GIF_PATH_KEY = "com.sunapp.simplegif.PATH1";
    private static final String GIF_FRAME_KEY = "com.sunapp.simplegif.FRAME1";
    private static final String GIF_DELAY_KEY = "com.sunapp.simplegif.DELAY1";
    private static final String GIF_NUMBER_KEY = "com.sunapp.simplegif.NUMBER1";
    private static final String TAG = "SGwidProvider";

    AlarmManager alarm;
    static String pathToUse;
    static long updateTime;
    BroadcastReceiver screenoffReceiver;
    IntentFilter filter;
    public int iupdateWids;

    public SimpleGifWidgetProvider() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Received something");
        if (filter == null) {
            filter = new IntentFilter();
        }

        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);

        screenoffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

                    Intent in3 = new Intent(context, SimpleGifDecodeService.class);
                    context.stopService(in3);

                }

                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    Intent in2 = new Intent(context, SimpleGifDecodeService.class);
                    context.stopService(in2);

                }
            }
        };

        context.getApplicationContext().registerReceiver(screenoffReceiver, filter);
        super.onReceive(context, intent);
    }



    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Log.i(TAG,"opts changed");
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    public static void setters(String path, long time) {
        pathToUse = path;
        updateTime = time;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        Log.i(TAG, "Widget update called");

        iupdateWids = 0;
        int[] wids;
        AppWidgetManager am = AppWidgetManager.getInstance(context);
        wids = am.getAppWidgetIds(new ComponentName(context, SimpleGifWidgetProvider.class));

        for (int id : wids) {
            iupdateWids++;
            Log.i(TAG, "id numbers: " + id);
        }

        context.getApplicationContext().registerReceiver(screenoffReceiver, filter);

        Intent in = new Intent(context, SimpleGifDecodeService.class);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        pathToUse = preferences.getString(GIF_PATH_KEY, "");

        in.putExtra(FILE_KEY, pathToUse);
        in.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

        if (!pathToUse.equals(""))
            context.startService(in);

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

        int inumwids = 0;
        int[] wids;
        AppWidgetManager am = AppWidgetManager.getInstance(context);
        wids = am.getAppWidgetIds(new ComponentName(context, SimpleGifWidgetProvider.class));

        for (int id : wids) {
            inumwids++;
        }

        if (inumwids == 0) {

            alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent in2 = new Intent(context, SimpleGifDecodeService.class);
            in2.putExtra(FILE_KEY, pathToUse);
            in2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            context.stopService(in2);

        }
        super.onDeleted(context, appWidgetIds);
    }
}