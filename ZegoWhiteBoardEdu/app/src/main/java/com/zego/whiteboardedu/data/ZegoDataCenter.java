package com.zego.whiteboardedu.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import com.zego.whiteboardedu.application.BaseApplication;
import com.zego.whiteboardedu.manager.entity.ZegoUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**
 * Zego App ID 和 Sign Data，需从Zego主页申请。 //TODO
 */
public class ZegoDataCenter {

    private static final String SP_NAME = "sp_name_base";
    private static final String SP_KEY_USER_ID = "sp_key_user_id";
    private static final String SP_KEY_USER_NAME = "sp_key_user_name";

    public static final long UDP_APP_ID = 1082937486L;


    public static final byte[] UDP_SIGN_DATA = new byte[]{
            (byte) 0x60, (byte) 0x25, (byte) 0xbf, (byte) 0x4c, (byte) 0xe1, (byte) 0x10, (byte) 0x42, (byte) 0xda,
            (byte) 0x3a, (byte) 0xa0, (byte) 0xf6, (byte) 0xab, (byte) 0xf8, (byte) 0x14, (byte) 0x9e, (byte) 0xcc,
            (byte) 0x48, (byte) 0x8a, (byte) 0x34, (byte) 0x08, (byte) 0x45, (byte) 0xac, (byte) 0x60, (byte) 0x77,
            (byte) 0x25, (byte) 0x1c, (byte) 0x00, (byte) 0x63, (byte) 0x4b, (byte) 0xc6, (byte) 0xd6, (byte) 0x9a
    };

    public static final ZegoUser ZEGO_USER = new ZegoUser(); // 根据自己情况初始化唯一识别USER

    static {
        ZEGO_USER.userID = getUserID(); // 使用 SERIAL 作为用户的唯一识别
        ZEGO_USER.userName = getUserName();
    }

    /**
     * 获取保存的UserName，如果没有，则新建
     */
    private static String getUserID() {
        SharedPreferences sp = BaseApplication.sApplication.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String userID = sp.getString(SP_KEY_USER_ID, "");
        if (TextUtils.isEmpty(userID)) {
            userID = UUID.randomUUID().toString();
            // 保存用户名
            sp.edit().putString(SP_KEY_USER_ID, userID).apply();
        }
        return userID;
    }

    /**
     * 获取保存的UserName，如果没有，则新建
     */
    private static String getUserName() {
        SharedPreferences sp = BaseApplication.sApplication.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String userName = sp.getString(SP_KEY_USER_NAME, "");
        if (TextUtils.isEmpty(userName)) {
            String monthAndDay = new SimpleDateFormat("MMdd", Locale.CHINA).format(new Date());
            // 以设备名称 + 时间日期 + 一位随机数  作为用户名
            userName = Build.MODEL + monthAndDay + new Random().nextInt(10);
            // 保存用户名
            sp.edit().putString(SP_KEY_USER_NAME, userName).apply();
        }
        return userName;
    }
}
