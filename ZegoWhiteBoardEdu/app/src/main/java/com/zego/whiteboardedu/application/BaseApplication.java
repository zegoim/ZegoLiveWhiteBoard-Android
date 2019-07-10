package com.zego.whiteboardedu.application;

import android.app.Application;
import android.content.Context;

public class BaseApplication extends Application {

    private final static String TAG = BaseApplication.class.getSimpleName();

    public static Context sApplication;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sApplication = base;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }
}
