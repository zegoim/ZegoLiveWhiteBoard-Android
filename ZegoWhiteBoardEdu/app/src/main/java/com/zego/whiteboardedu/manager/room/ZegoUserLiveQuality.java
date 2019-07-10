package com.zego.whiteboardedu.manager.room;

import com.zego.zegoliveroom.entity.ZegoPlayStreamQuality;
import com.zego.zegoliveroom.entity.ZegoPublishStreamQuality;

public final class ZegoUserLiveQuality {

    /**
     * 视频帧率(编码/网络发送)
     */
    public double mVideoFPS;
    /**
     * 视频码率(kb/s)
     */
    public double mVideoKbps;
    /**
     * 音频码率(kb/s)
     */
    public double mAudioKbps;
    /**
     * 延时(ms)
     */
    public int mRtt;
    /**
     * 丢包率(0.0~100.0)
     */
    public float mPacketLoss;
    /**
     * 质量(0~3)
     */
    public int mNetQuality;
    /**
     * 语音延迟(ms)
     */
    public int mAudioDelay;

    public static ZegoUserLiveQuality initWithPublishQuality(ZegoPublishStreamQuality publishStreamQuality) {
        ZegoUserLiveQuality quality = new ZegoUserLiveQuality();
        quality.mVideoFPS = publishStreamQuality.vnetFps;
        quality.mVideoKbps = publishStreamQuality.vkbps;
        quality.mAudioKbps = publishStreamQuality.akbps;
        quality.mRtt = publishStreamQuality.rtt;
        quality.mPacketLoss = (float) publishStreamQuality.pktLostRate * 100 / 255;
        quality.mNetQuality = publishStreamQuality.quality;
        quality.mAudioDelay = 0;
        return quality;
    }

    public static ZegoUserLiveQuality initWithPlayQuality(ZegoPlayStreamQuality playStreamQuality) {
        ZegoUserLiveQuality quality = new ZegoUserLiveQuality();
        quality.mVideoFPS = playStreamQuality.vrndFps;
        quality.mVideoKbps = playStreamQuality.vkbps;
        quality.mAudioKbps = playStreamQuality.akbps;
        quality.mRtt = playStreamQuality.rtt;
        quality.mPacketLoss = (float) playStreamQuality.pktLostRate * 100 / 255;
        quality.mNetQuality = playStreamQuality.quality;
        quality.mAudioDelay = playStreamQuality.delay;
        return quality;
    }

    @Override
    public String toString() {
        return "ZegoUserLiveQuality{" +
                "mVideoFPS=" + mVideoFPS +
                ", mVideoKbps=" + mVideoKbps +
                ", mAudioKbps=" + mAudioKbps +
                ", mRtt=" + mRtt +
                ", mPacketLoss=" + mPacketLoss +
                ", mNetQuality=" + mNetQuality +
                ", mAudioDelay=" + mAudioDelay +
                '}';
    }
}
