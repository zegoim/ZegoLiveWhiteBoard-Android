package com.zego.whiteboardedu.manager.room;

/**
 * 房间登录相关事件
 */
public final class ZegoRoomLoginEvent {
    /**
     * 登录
     */
    public final static int LOGIN = 0;
    /**
     * 登录成功
     */
    public final static int LOGIN_SUCCESS = 1;
    /**
     * 登录失败
     */
    public final static int LOGIN_FAILED = 2;
    /**
     * 登出
     */
    public final static int LOGOUT = 3;
    /**
     * 临时断连
     */
    public final static int TEMP_BROKE = 4;
    /**
     * 临时断连后的重连成功
     */
    public final static int RECONNECT = 5;
    /**
     * 网络断开
     */
    public final static int DISCONNECT = 6;
    /**
     * 被踢出，可能发生事件：
     * 1、同一用户多点登录被踢出
     * 2、服务器主动踢出用户
     */
    public final static int KICK_OUT = 7;
}