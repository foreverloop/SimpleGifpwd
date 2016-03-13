package com.ex.sunapp.simplegif;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.*;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleGifDecodeService extends Service {

    private String mPath;
    private int mAppWidId;
    private static final String PATH_KEY = "com.ex.sunapp.PATH_KEY";
    private static String WID_ID_KEY = "com.ex.sunapp.WID_ID_KEY";
    private static String GIF_PATH_KEY = "com.sunapp.simplegif.PATH";
    private ConcurrentHashMap<Integer,String> mPathMap;
    private HashMap<Integer,GifView> mGifViewMap;
    private HashMap<Integer,Integer> mDelayMap;
    private HashMap<Integer,Integer> mHeightMap;
    private HashMap<Integer,Integer> mWidthMap;
    public GifView mGifView;
    String LastHeapWith;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private int mDelay;
    public static Boolean sRunning = false;


    /**
     * method declaring a custom runnable which takes an argument
     *  of the widget number we're operating on
     *  and returns a runnable for use later on this widget
     *  could be compared to a callback
     */
    private Runnable runDecodeGif(final int widgetNo){

        class newDecodeGif implements Runnable{

            int widno;
            newDecodeGif(int widgetNo){
                widno = widgetNo;
            }

            @Override
            public void run() {

                mServiceHandler.removeCallbacks(this);
                String path = mPathMap.get(widno);
                File fi = new File(path);
                if(fi.exists()) {
                    getBitmap(path, widno);
                } else {
                    Resources res = getResources();

                    Toast.makeText(getApplicationContext(),
                            res.getString(R.string.error_file_not_found, fi.toString()),
                            Toast.LENGTH_LONG).show();

                    updateSharedPrefs(mPathMap.get(widgetNo), 0, widno);

                    if(mPathMap != null)
                        mPathMap.remove(widno);
                    if(mDelayMap != null)
                        mDelayMap.remove(widno);
                    if(mHeightMap != null)
                        mHeightMap.remove(widno);
                    if(mWidthMap != null)
                        mWidthMap.remove(widno);
                    if(mGifViewMap != null)
                        mGifViewMap.remove(widno);

                    //no more active gifs
                    if(mPathMap.size() == 0)
                        stopSelf();
                }

                if(mDelayMap != null && widno  > 0)
                    mDelay = mDelayMap.get(widno);
                if(mServiceHandler != null) {
                    if(mDelay == 0)
                        mDelay = 100;
                    mServiceHandler.postDelayed(this, mDelay);
                }

            }
        }

        return new newDecodeGif(widgetNo);
    }

    /**
     * Handler that receives messages from the thread
     * */
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
    }

    /**
     * check Hashmaps exist and create if required
     * */
    private void checkMaps(){
        if(mGifViewMap == null)
            mGifViewMap = new HashMap<>();

        if(mDelayMap == null)
            mDelayMap = new HashMap<>();

        if(mHeightMap == null)
            mHeightMap = new HashMap<>();

        if(mWidthMap == null)
            mWidthMap = new HashMap<>();
    }

    private void cleanUpMaps(int[] widgetIds){

        ArrayList<Integer> nonPrim = new ArrayList<>();

        for(int i : widgetIds){
            nonPrim.add(i);
        }

        for(int key : mPathMap.keySet()) {

            if(nonPrim.contains(key))
                continue;

            if(mPathMap != null)
                mPathMap.remove(key);
            if(mDelayMap != null)
                mDelayMap.remove(key);
            if(mHeightMap != null)
                mHeightMap.remove(key);
            if(mWidthMap != null)
                mWidthMap.remove(key);
            if(mGifViewMap != null)
                mGifViewMap.remove(key);

        }
    }

    /**
     * verify Gif image is not larger than screen, should prevent some oom errors
     */
    private boolean verifySafeSize(String path){

        DisplayMetrics displaymetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) this.getSystemService(Service.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        int height = displaymetrics.heightPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;

        //if oom errors, this could be added as width / 2 and height / 2
        if(imageWidth >= width){
            return false;
        } else if (imageHeight >= height){
            return false;
        }

        return true;
    }

    /**
     * called only off background thread, decodes Gif and updates widgets with relevant image
     */
    private void getBitmap(String path, int widId){

        int prevHeight = 1;
        int prevWidth = 1;
        Bitmap b;
        Bitmap icon;
        checkMaps();

        if(mGifViewMap.get(widId) == null){
            mGifView = new GifView(getApplicationContext());
            mGifViewMap.put(widId,mGifView);

            if(verifySafeSize(path)) {
                try {
                    icon = BitmapFactory.decodeFile(path);
                    prevHeight = icon.getHeight();
                    prevWidth = icon.getWidth();
                    mHeightMap.put(widId, prevHeight);
                    mWidthMap.put(widId, prevWidth);
                    mGifView.setGif(path, icon);
                    mGifView.decode();
                } catch (OutOfMemoryError oom) {
                    Toast.makeText(this, R.string.oom, Toast.LENGTH_LONG).show();
                    return;
                }
            }

        } else {
            mGifView = mGifViewMap.get(widId);
            prevHeight = mHeightMap.get(widId);
            prevWidth = mWidthMap.get(widId);
        }

        LastHeapWith = path;

        if(mGifView == null)
            return;

        while (mGifView.decodeStatus != 2){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(!mDelayMap.containsKey(widId)) {
            mDelay = mGifView.getDelayTime(mGifView.getFrameNum() - 1);
            mDelayMap.put(widId, mDelay);
        } else {
            mDelay = mDelayMap.get(widId);
        }

        mGifView.destroyDrawingCache();
        mGifView.setDrawingCacheEnabled(true);
        mGifView.nextFrame();
        mGifView.invalidate();
        mGifView.buildDrawingCache(true);

        Bitmap bit = Bitmap.createBitmap(prevWidth,prevHeight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bit);

        mGifView.layout(0, 0, mGifView.getWidth() , mGifView.getHeights() );
        mGifView.draw(c);
        b = bit;

        final AppWidgetManager awm = AppWidgetManager.getInstance(getApplicationContext());
        int[] appWidgetIDs = awm.getAppWidgetIds(new ComponentName(getApplicationContext(), SimpleGifWidgetProvider.class));

        final RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_template);

        if(appWidgetIDs != null)
            views.setImageViewBitmap(R.id.img_gif_wd,b);

        awm.updateAppWidget(widId, views);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //required as part of extending service class
        return null;
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sRunning = true;
        String path;
        int[] widgets;
        AppWidgetManager aw = AppWidgetManager.getInstance(getApplicationContext());

        aw.updateAppWidget(aw.getAppWidgetIds(new ComponentName(getApplicationContext(), SimpleGifWidgetProvider.class)),
                new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_template));

        widgets = aw.getAppWidgetIds(new ComponentName(this, SimpleGifWidgetProvider.class));
        if(intent != null) {
            mAppWidId = intent.getIntExtra(WID_ID_KEY, 0);
            path = PATH_KEY + mAppWidId;
            mPath = intent.getStringExtra(path);
        }

        //if we came from a broadcast
        if(mAppWidId == 0) {
            if(mPathMap == null)
                mPathMap = new ConcurrentHashMap<>();
            widgets = aw.getAppWidgetIds(new ComponentName(this, SimpleGifWidgetProvider.class));

            for(int wid : widgets){
                mPathMap.put(wid, findExistingWidgets(wid));
                mServiceHandler.postDelayed(runDecodeGif(wid),100);
            }
        } else {
            if(mPathMap == null)
                mPathMap = new ConcurrentHashMap<>();

            if (mAppWidId > 0) {
                Log.i("serv","begins");
                mPathMap.put(mAppWidId, mPath);
                mServiceHandler.postDelayed(runDecodeGif(mAppWidId),100);
                updateSharedPrefs(mPath,1,mAppWidId);
            }
            cleanUpMaps(widgets);
        }

        Intent ReceIntent = new Intent(getApplicationContext(),SimpleGifWidgetProvider.class);
        getApplicationContext().sendBroadcast(ReceIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        sRunning = false;
        mServiceHandler.removeCallbacksAndMessages(null);
        mServiceHandler = null;
    }

    private void updateSharedPrefs(String path, int mode, int widgetno){
        //mode = 0 to remove, 1 to add/update
        final String pathKey = GIF_PATH_KEY + String.valueOf(widgetno);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        pref.getAll();
        if(mode == 1){
            editor.putString(pathKey, path);
            editor.apply();
        } else if (mode == 0){
            editor.remove(pathKey);
            editor.apply();
        }
    }

    private String findExistingWidgets(int widgetId){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        final String pathKey = GIF_PATH_KEY + String.valueOf(widgetId);
        return pref.getString(pathKey,"");
    }
}

