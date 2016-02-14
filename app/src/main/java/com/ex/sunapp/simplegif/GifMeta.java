package com.ex.sunapp.simplegif;

public class GifMeta {

    private String mFileName;
    private int mFrames;
    private int mRefreshRate;
    private int mWidgetNo;
    private int mHeight;
    private int mWidth;

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getWidgetNo() {
        return mWidgetNo;
    }

    public void setWidgetNo(int widgetNo) {
        mWidgetNo = widgetNo;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public int getFrames() {
        return mFrames;
    }

    public void setFrames(int frames) {
        mFrames = frames;
    }

    public int getRefreshRate() {
        return mRefreshRate;
    }

    public void setRefreshRate(int refreshRate) {
        mRefreshRate = refreshRate;
    }
}