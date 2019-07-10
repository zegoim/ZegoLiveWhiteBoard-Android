package com.zego.whiteboardedu.manager.entity;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 对LiveRoomSDK 中 ZegoUser功能扩展
 */
public class ZegoUser implements Cloneable {

    private final static String TAG = ZegoUser.class.getSimpleName();

    // ZegoUser 各字段的简写 key
    private final static String ZEGO_USER_ID_KEY = "i";
    private final static String ZEGO_USER_NAME_KEY = "n";

    /**
     * 用户ID，只支持数字，字母，下划线，长度为1~64字节，需在同一 AppID 下全局唯一
     */
    public String userID;
    /**
     * 用户名
     * <p>
     * 不能为 null 或者 空字符串("")，长度为1~255字节
     */
    public String userName;

    /**
     * 返回当前对象的 userID 和 userName 值是否有效
     *
     * @return true表示有效，false表示无效。
     */
    public boolean isValid() {
        return !TextUtils.isEmpty(userID) && !TextUtils.isEmpty(userName);
    }

    @Nullable
    public static ZegoUser userFromJsonObject(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        try {
            ZegoUser zegoUser = new ZegoUser();
            zegoUser.userID = jsonObject.getString(ZEGO_USER_ID_KEY);
            zegoUser.userName = jsonObject.getString(ZEGO_USER_NAME_KEY);
            return zegoUser;
        } catch (JSONException e) {
            Log.e(TAG, "zegoUserFromJsonObject " + e.getMessage());
            return null;
        }
    }

    @Nullable
    public JSONObject jsonObject() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ZEGO_USER_ID_KEY, userID);
            jsonObject.put(ZEGO_USER_NAME_KEY, userName);
            return jsonObject;
        } catch (JSONException e) {
            Log.e(TAG, "jsonObject " + e.getMessage());
            return null;
        }
    }

    @Override
    public int hashCode() {
        return isValid() ? userID.hashCode() : -1;
    }

    @Override
    public boolean equals(Object obj) {
        return isValid() && (obj instanceof ZegoUser && hashCode() == obj.hashCode());
    }

    @Override
    public ZegoUser clone() {
        try {
            return (ZegoUser) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("CloneNotSupportedException ??");
        }
    }


    @Override
    public String toString() {
        return "ZegoUser{" +
                "userID='" + userID + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }

    public com.zego.zegoliveroom.entity.ZegoUser toInnerZegoUser() {
        com.zego.zegoliveroom.entity.ZegoUser user = new com.zego.zegoliveroom.entity.ZegoUser();
        user.userID = userID;
        user.userName = userName;
        return user;
    }
}
