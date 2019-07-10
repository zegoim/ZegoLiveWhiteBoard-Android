package com.zego.whiteboardedu.manager.room;

import com.zego.whiteboardedu.manager.entity.ResultCode;
import com.zego.whiteboardedu.manager.entity.ZegoUser;

/**
 * 推拉流状态管理基本状态更新回调
 */
public interface ZegoRoomManagerCallback {

    /**
     * 登录相关事件回调
     *
     * @param event     登录事件，对应ZegoRoomLoginEvent
     * @param status    事件后的当前状态，对应ZegoRoomLoginStatus
     * @param errorCode 错误码
     */
    void onLoginEventOccur(int event, int status, ResultCode errorCode);

    /**
     * 用户开始推拉流回调
     *
     * @param user 开始推拉流的用户
     */
    void onLiveUserJoin(ZegoUser user);

    /**
     * 用户停止推拉流回调
     *
     * @param user 停止推拉流的用户
     */
    void onLiveUserLeave(ZegoUser user);

    /**
     * 当 ZegoRoomManager 自动重连逻辑因情况停止后，会触发该回调
     *
     * @param stopReason 自动重连停止原因，详情参考 {@link ZegoRoomReconnectStopReason}
     */
    void onAutoReconnectStop(int stopReason);

    /**
     * 接收房间信令回调
     *
     * @param content  信令内容
     * @param fromUser 发消息的用户
     */
    void onRecvCustomCommand(ZegoUser fromUser, String content);
}
