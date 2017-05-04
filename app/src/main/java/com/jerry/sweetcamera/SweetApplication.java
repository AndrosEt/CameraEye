package com.jerry.sweetcamera;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import com.jerry.sweetcamera.util.FileUtil;

/**
 * @author jerry
 * @date 2016/03/11
 */
public class SweetApplication extends Application {

    public static int mScreenWidth = 0;
    public static int mScreenHeight = 0;

    public static SweetApplication CONTEXT;

    private Bitmap mCameraBitmap;

    @Override
    public void onCreate() {
        super.onCreate();

        // DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        DisplayMetrics mDisplayMetrics = getApplicationContext().getResources()
                .getDisplayMetrics();
        SweetApplication.mScreenWidth = mDisplayMetrics.widthPixels;
        SweetApplication.mScreenHeight = mDisplayMetrics.heightPixels;

        CONTEXT = this;

        FileUtil.initFolder();
    }

    public Bitmap getCameraBitmap() {
        return mCameraBitmap;
    }

    public void setCameraBitmap(Bitmap mCameraBitmap) {
        if (mCameraBitmap != null) {
            recycleCameraBitmap();
        }
        this.mCameraBitmap = mCameraBitmap;
    }

    public void recycleCameraBitmap() {
        if (mCameraBitmap != null) {
            if (!mCameraBitmap.isRecycled()) {
                mCameraBitmap.recycle();
            }
            mCameraBitmap = null;
        }
    }
}
