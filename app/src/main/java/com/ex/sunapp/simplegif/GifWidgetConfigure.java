package com.ex.sunapp.simplegif;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class GifWidgetConfigure extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    public static ArrayList<GifMeta> mGifMetaArrayList;
    private Toolbar mToolbar;
    private static final int READ_EXTERNAL_REQUEST = 12;
    private int mAppWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gif_config_activity);
        setResult(Activity.RESULT_CANCELED);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        Log.i(TAG,"true id: " + mAppWidgetId);

        mToolbar = (Toolbar) findViewById(R.id.app_b);
        mToolbar.setTitle(R.string.app_name);
        mToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_white_24dp);
        if(Build.VERSION.SDK_INT >= 21) {
            mToolbar.setElevation(8);
        }

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //create LRU cache to improve efficiency
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_REQUEST);
        } else {
            if (savedInstanceState == null)
                new FileSearchTask().execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case READ_EXTERNAL_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new FileSearchTask().execute();
                } else {
                    Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    class FileSearchTask extends AsyncTask<Void,Void,ArrayList<GifMeta>> {

        @Override
        protected ArrayList<GifMeta> doInBackground(Void... params) {
            AutoSearch as = new AutoSearch(getApplicationContext());
            return as.getGifList();
        }

        @Override
        protected void onPostExecute(ArrayList<GifMeta> gifList) {
            super.onPostExecute(gifList);

            mGifMetaArrayList = gifList;

            FragmentManager fm = getSupportFragmentManager();
            AutoSearchFragment asf = AutoSearchFragment.newInstance(mAppWidgetId);
            fm.beginTransaction().add(R.id.fragment_container_search,asf).commit();
        }
    }

}
