package com.zego.whiteboardedu.manager.room;

/**
 * 房间登录状态常量
 */
public final class ZegoRoomLoginStatus {
    /**
     * 登出状态
     */
    public final static int LOGOUT = 0;
    /**
     * 正在登录状态
     */
    public final static int START_LOGIN = 1;
    /**
     * 已登录状态
     */
    public final static int LOGIN = 2;
    /**
     * 临时断连状态
     */
    public final static int TEMP_BROKE = 3;
}