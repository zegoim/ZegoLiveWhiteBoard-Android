package com.zego.whiteboardedu.manager;


import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.zego.whiteboardedu.data.ZegoDataCenter;
import com.zego.zegoliveroom.ZegoLiveRoom;
import com.zego.zegoliveroom.callback.IZegoInitSDKCompletionCallback;
import com.zego.zegoliveroom.constants.ZegoAvConfig;


/**
 * des: zego api管理器.
 */
public class ZegoApiManager {

    private static final String TAG = ZegoApiManager.class.getSimpleName();

    private ZegoLiveRoom mZegoLiveRoom;

    private ZegoApiManager() {
        mZegoLiveRoom = new ZegoLiveRoom();
    }

    private final static class ZegoApiManagerHolder {
        private static ZegoApiManager sInstance = new ZegoApiManager();
    }

    public static ZegoApiManager getInstance() {
        return ZegoApiManagerHolder.sInstance;
    }

    /**
     * 初始化sdk.
     *
     * @param context ApplicationContext 上下文
     */
    public void initSDK(Application context) {
        //设置参数
        init(context);
    }

    private void init(final Application context) {
        ZegoLiveRoom.SDKContext sdkContext = new ZegoLiveRoom.SDKContext() {
            @Nullable
            @Override
            public String getSoFullPath() {
                return null;
            }

            @Nullable
            @Override
            public String getLogPath() {
                return null;
            }

            @NonNull
            @Override
            public Application getAppContext() {
                return context;
            }
        };

        // 设置sdk context 对象，必须要在使用 ZegoLiveRoom 相关方法之前调用，因为里面有so库的加载
        ZegoLiveRoom.setSDKContext(sdkContext);
        // 初始化sdk
        Log.d(TAG, "-->:: initSDK start");
        boolean ret = mZegoLiveRoom.initSDK(ZegoDataCenter.UDP_APP_ID, ZegoDataCenter.UDP_SIGN_DATA, new IZegoInitSDKCompletionCallback() {
            @Override
            public void onInitSDK(int errorCode) {
                Log.d(TAG, "-->:: onInitSDK errorCode = " + errorCode);
            }
        });
        mZegoLiveRoom.setRoomConfig(true, true);
        // 用户需按需设置推流配置
        ZegoAvConfig zegoAvConfig = new ZegoAvConfig(ZegoAvConfig.Level.Generic);
        mZegoLiveRoom.setAVConfig(zegoAvConfig);

        // 设置APP朝向为横屏
        mZegoLiveRoom.setAppOrientation(Surface.ROTATION_90);

        if (!ret) {
            // sdk初始化失败
            Toast.makeText(context, "Zego SDK初始化失败!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * @return 返回 ZegoLiveRoom 实例
     */
    public ZegoLiveRoom getZegoLiveRoom() {
        return mZegoLiveRoom;
    }
}
