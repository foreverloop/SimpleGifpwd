package com.ex.sunapp.simplegif;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import java.util.ArrayList;

public class AutoSearch {

    private ArrayList<GifMeta> mGifMetaArrayList;
    private Context mContext;

    public AutoSearch(Context context){
        mContext = context;
    }

    public ArrayList<GifMeta> getGifList(){
        mGifMetaArrayList = new ArrayList<>();
        findAllGifs();
        return mGifMetaArrayList;
    }


    private void findAllGifs(){

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE
                + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

        String[] projection = {MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA };

        Cursor cursor = null;

        try {
            Uri queryUri = MediaStore.Files.getContentUri("external");
            cursor = mContext.getContentResolver().query(queryUri, projection, selection, null, null);

            if(cursor != null){

                cursor.moveToFirst();

                while(!cursor.isAfterLast()){

                    String pathString  = cursor.getString(1);
                    if(pathString.endsWith(".gif")){
                        //Consider not adding if gif already exists but for a different path
                        GifMeta gm = new GifMeta();
                        gm.setFileName(pathString);
                        mGifMetaArrayList.add(gm);
                    }
                    cursor.moveToNext();
                }
            }

        } catch (Exception e) {
            System.out.println("failed to read images: " + e);
        }finally{
            if( cursor != null){
                cursor.close();
            }
        }
    }
}