package com.zego.whiteboardedu.manager.room;

public final class ZegoRoomReconnectStopReason {

    /**
     * 入参不合法
     */
    public final static int Param = 0;

    /**
     * 手动调用leaveRoom终止重连
     */
    public final static int LOGOUT = 1;

    /**
     * 被服务器踢出终止重连
     */
    public final static int KICK_OUT = 2;

    /**
     * 重连超时
     */
    public final static int TIME_OUT = 3;
}
