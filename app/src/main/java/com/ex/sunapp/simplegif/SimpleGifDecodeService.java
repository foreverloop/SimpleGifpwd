package com.ex.sunapp.simplegif;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.*;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimpleGifDecodeService extends Service {

    private String mPath;
    private int mAppWidId;
    private static String PATH_KEY = "com.ex.sunapp.PATH_KEY";
    private static String WID_ID_KEY = "com.ex.sunapp.WID_ID_KEY";
    private HashMap<Integer,String> mPathMap;
    public GifView mGifView;
    public AppWidgetManager app;
    Bitmap b;
    Bitmap icon;
    Bitmap scaled;
    String LastHeapWith;
    private String sharedPath;
    int prevHeight;
    int prevWidth;
    int curHeight;
    int curWidth;
    int numappwids;
    int inumtimes;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private static final String GIF_PATH_KEY = "com.sunapp.simplegif.PATH1";
    private static final String GIF_DELAY_KEY = "com.sunapp.simplegif.DELAY1";
    private static String GIF_HEIGHT_KEY = "com.sunapp.simplegif.HEIGHT1";
    private static String GIF_WIDTH_KEY = "com.sunapp.simplegif.WIDTH1";
    private String mSharedPath;
    private static final String TAG = "DecodeService";

    private Runnable DecodeGif = new Runnable() {
        @Override
        public void run() {

            mServiceHandler.removeCallbacks(this);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            //mSharedPath = preferences.getString(GIF_PATH_KEY, "");

            //if(!mPath.equals("")) {
            //    File f = new File(mPath);
               // if (f.exists()) {
                    //Log.i(TAG,"decoding...");
                    Iterator it = mPathMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Log.i(TAG, "in while loop...");
                        Map.Entry pair = (Map.Entry)it.next();
                        System.out.println(pair.getKey() + " = " + pair.getValue());
                        //it.remove(); // avoids a ConcurrentModificationException
                        File fi = new File(pair.getValue().toString());

                        if(fi.exists()) {
                            getBitmap(pair.getValue().toString(), (int) pair.getKey());
                        } else {
                                 Resources res = getResources();

                                 Toast.makeText(getApplicationContext(),
                                         res.getString(R.string.error_file_not_found, fi.toString()),
                                         Toast.LENGTH_LONG).show();
                        }
                    }


                    if(mServiceHandler != null) {
                        mServiceHandler.postDelayed(this, preferences.getInt(GIF_DELAY_KEY, 100));
                        Log.i(TAG, "repost");
                    }
                    //handle no iamge or valid handler
                //}
               // else {
               //     Resources res = getResources();

               //     Toast.makeText(getApplicationContext(), res.getString(R.string.error_file_not_found, mPath),
                //            Toast.LENGTH_LONG).show();
                    //return;
                }
            //}
       // }
    };

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
    }

    public void getBitmap(String path, int widId){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //if(!preferences.getString(GIF_PATH_KEY, "").equals("")) {
            //sharedPath = preferences.getString(GIF_PATH_KEY, "");
            sharedPath = path;
            if(!sharedPath.equals(LastHeapWith))
                mGifView = null;
        //}

        if(mGifView == null){

            mGifView = new GifView(getApplicationContext());

            if(prevWidth == 0)
                prevWidth = 80;
            if(prevHeight == 0)
                prevHeight = 80;
            Log.i(TAG,"not called every time");
            icon = AutoSearchFragment.decodeSampledBitmapFromResource(sharedPath,prevWidth,prevHeight);
            //icon = BitmapFactory.decodeFile(sharedPath);


            mGifView.setGif(sharedPath, icon);
            mGifView.decode();
        }

        LastHeapWith = sharedPath;

        while (mGifView.decodeStatus != 2) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mGifView.destroyDrawingCache();
        mGifView.setDrawingCacheEnabled(true);
        mGifView.nextFrame();
        mGifView.invalidate();
        mGifView.buildDrawingCache(true);

        //int twidth = preferences.getInt(GIF_WIDTH_KEY,400);
        //int theight = preferences.getInt(GIF_HEIGHT_KEY,300);

        //Bitmap bit = Bitmap.createBitmap(preferences.getInt(GIF_WIDTH_KEY, 400),
        //       preferences.getInt(GIF_HEIGHT_KEY, 300), Bitmap.Config.ARGB_8888);

        //Bitmap bit = Bitmap.createBitmap(400,
        //    300, Bitmap.Config.ARGB_8888);
        Log.i(TAG,"bitmap frame constructed");
        Bitmap bit = Bitmap.createBitmap(400,300,Bitmap.Config.ARGB_8888);
        //Bitmap bit = AutoSearchFragment.decodeSampledBitmapFromResource(mSharedPath,prevWidth,prevHeight);
        //Bitmap mutableBitmap = bit.copy(Bitmap.Config.ARGB_8888, true);

        Canvas c = new Canvas(bit);

        mGifView.layout(0, 0, mGifView.getMeasuredWidth(), mGifView.getMeasuredHeight());

        mGifView.draw(c);

        b = bit;
        bit = null;

        final AppWidgetManager appWidgetMan = AppWidgetManager.getInstance(getApplicationContext());

        int[] appWidgetIDs = appWidgetMan
                .getAppWidgetIds(new ComponentName(getApplicationContext(), SimpleGifWidgetProvider.class));

        final RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_template);

        if(appWidgetIDs != null) {

            for (int e = 0; e < appWidgetIDs.length; e++) {
                numappwids++;
                Bundle ops = appWidgetMan.getAppWidgetOptions(appWidgetIDs[e]);

                curHeight = ops.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                curWidth = ops.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);

                //if(curHeight < curWidth || curWidth > curHeight)
                if((prevHeight != curHeight) || (prevWidth != curWidth)){

                    //source was false
                    scaled = Bitmap.createScaledBitmap(b,(curWidth * 2),(curHeight * 2),true);
                    b = scaled;
                }

                prevHeight = ops.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                prevWidth = ops.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

                if(b != null)
                    views.setImageViewBitmap(R.id.img_gif_wd,b);
            }
        }

        //these concurrency issues are happens probably because of the shared variables
        appWidgetMan.updateAppWidget(widId,views);
        appWidgetMan.updateAppWidget(appWidgetIDs, views);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //decode gifs off UI thread, huge improvement over SimpleGifService
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        inumtimes++;
        AppWidgetManager aw = AppWidgetManager.getInstance(getApplicationContext());

        aw.updateAppWidget(aw.getAppWidgetIds(new ComponentName(getApplicationContext(), SimpleGifWidgetProvider.class)),
                new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_template));

        mAppWidId = intent.getIntExtra(WID_ID_KEY,0);
        PATH_KEY = PATH_KEY + mAppWidId;
        mPath = intent.getStringExtra(PATH_KEY);
        Log.i(TAG,"path: " + mPath);

        if(mPathMap == null)
            mPathMap = new HashMap<>();

        //try to serialize later
        mPathMap.put(mAppWidId,mPath);

        //add path we got to array, map or some sort of storage
        //wrap the calls to post delayed in a for each widget loop
        //pass in the paths as a variable, so the runnable runs seperately for each widget

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mServiceHandler.postDelayed(DecodeGif,preferences.getInt(GIF_DELAY_KEY,100));
        Intent ReceIntent = new Intent(getApplicationContext(),SimpleGifWidgetProvider.class);
        getApplicationContext().sendBroadcast(ReceIntent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        mServiceHandler.removeCallbacks(DecodeGif);
        mServiceHandler = null;
    }


}
