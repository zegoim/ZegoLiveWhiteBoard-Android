package com.zego.whiteboardedu.manager.room;

/**
 * 直播状态值常量
 */
public final class ZegoUserLiveStatus {
    /**
     * 未连接直播
     */
    public final static int WAIT_CONNECT = 0;
    /**
     * 正在请求直播连接
     */
    public final static int CONNECTING = 1;
    /**
     * 直播已连接
     */
    public final static int LIVE = 2;
}