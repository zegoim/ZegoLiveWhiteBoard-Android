package com.zego.whiteboardedu.manager.room;

import android.view.View;

import com.zego.zegoliveroom.constants.ZegoVideoViewMode;

public class ZegoUserLiveInfo {

    /**
     * 流状态，对应 {@link ZegoUserLiveStatus}
     */
    private int mStreamStatus;

    /**
     * 直播状态，对应 {@link ZegoUserLiveStatus}
     */
    private int mLiveStatus;

    /**
     * 首帧
     */
    public boolean mFirstFrame;

    /**
     * 声浪大小
     */
    public float mSoundLevel;

    /**
     * 扩展消息
     */
    public String mExtraInfo = "";

    /**
     * 直播状态
     */
    public ZegoUserLiveQuality mLiveQuality;

    public View mVideoView;

    /**
     * 视图模式
     * 详情参考 {@link ZegoVideoViewMode}
     */
    public int mViewMode;

    /**
     * 获取当前连接状态，对应 {@link ZegoUserLiveStatus}
     *
     * @return 当前连接状态
     */
    public int getStatus() {
        if (mStreamStatus == ZegoUserLiveStatus.LIVE) {
            return mLiveStatus;
        }
        return mStreamStatus;
    }

    public void setStreamStatus(int streamStatus) {
        if (mStreamStatus == streamStatus) {
            return;
        }

        mStreamStatus = streamStatus;

        if (streamStatus == ZegoUserLiveStatus.LIVE) {
            mLiveStatus = ZegoUserLiveStatus.LIVE;
        }
    }

    public int getStreamStatus() {
        return mStreamStatus;
    }

    public void setLiveStatus(int liveStatus) {
        if (mLiveStatus == liveStatus) {
            return;
        }
        mLiveStatus = liveStatus;
    }

    public int getLiveStatus() {
        return mLiveStatus;
    }
}