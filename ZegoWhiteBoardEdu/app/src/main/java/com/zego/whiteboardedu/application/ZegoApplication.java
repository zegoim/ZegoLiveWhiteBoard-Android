package com.zego.whiteboardedu.application;

import android.content.Context;

import com.zego.whiteboardedu.data.ZegoDataCenter;
import com.zego.whiteboardedu.manager.ZegoApiManager;


public class ZegoApplication extends BaseApplication {

    @Override
    protected void attachBaseContext(final Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化 Zego LiveRoom SDK
        ZegoApiManager.getInstance().initSDK(this);
    }
}
