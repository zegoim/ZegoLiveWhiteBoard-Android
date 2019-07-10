package com.zego.whiteboardedu.manager.room;

import com.zego.whiteboardedu.manager.entity.ResultCode;
import com.zego.whiteboardedu.manager.entity.ZegoUser;

/**
 * 推拉流事件更新更新回调
 */
public interface ZegoRoomManagerLiveStatusCallback {

    /**
     * 推拉流事件状态更新
     *
     * @param user       流对应的用户
     * @param liveStatus 流的状态，对应ZegoUserLiveStatus
     * @param errorCode  错误码
     */
    void onLiveStatusChange(ZegoUser user, int liveStatus, ResultCode errorCode);

    /**
     * 当获取到视频首帧的时候回调
     *
     * @param user   视频对应的用户
     * @param width  视频宽度
     * @param height 视频高度
     */
    void onUserGetFirstFrame(ZegoUser user, int width, int height);

    /**
     * 当用户视频分辨率大小改变的时候回调。
     *
     * @param user   视频对应的用户
     * @param width  视频宽度
     * @param height 视频高度
     */
    void onUserVideoFrameChange(ZegoUser user, int width, int height);

    /**
     * 定时获取声浪回调
     *
     * @param user       流对应的用户
     * @param soundLevel 声浪大小
     */
    void onGetSoundLevel(ZegoUser user, float soundLevel);

    /**
     * 拉流的扩展信息更新回调
     *
     * @param user 流对应的用户
     */
    void onExtraInfoUpdate(ZegoUser user, String extraInfo);

    /**
     * 流质量信息更新回调
     *
     * @param user    流对应的用户
     * @param quality 流质量信息
     */
    void onLiveQualityUpdate(ZegoUser user, ZegoUserLiveQuality quality);
}