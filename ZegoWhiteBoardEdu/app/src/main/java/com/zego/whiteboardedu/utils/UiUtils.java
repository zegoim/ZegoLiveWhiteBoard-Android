package com.zego.whiteboardedu.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.zego.whiteboardedu.application.BaseApplication;

import java.lang.reflect.Field;


/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class UiUtils {
    @TargetApi(19)
    public static boolean setImmersedWindow(Window window, boolean immersive) {
        boolean result = false;
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();

            if (Build.VERSION.SDK_INT < 19) {
                try {
                    int trans_status = 64;
                    Field flags = lp.getClass().getDeclaredField("meizuFlags");
                    flags.setAccessible(true);
                    int value = flags.getInt(lp);
                    if (immersive) {
                        value |= trans_status;
                    } else {
                        value &= ~trans_status;
                    }

                    flags.setInt(lp, value);
                    result = true;
                } catch (Exception var7) {
                    Log.e("StatusBar", "setImmersedWindow: failed");
                }
            } else {
                lp.flags |= 67108864; // WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR
                window.setAttributes(lp);
                result = true;
            }
        }

        return result;
    }

    public static void setFullScreen(Window window) {
        int flags = 256 //View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | 512 //View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | 1024 //View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | 4; //View.SYSTEM_UI_FLAG_FULLSCREEN;
        flags |= 0x00001000;
        window.getDecorView().setSystemUiVisibility(flags);
    }

    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                BaseApplication.sApplication.getResources().getDisplayMetrics());
    }
}
