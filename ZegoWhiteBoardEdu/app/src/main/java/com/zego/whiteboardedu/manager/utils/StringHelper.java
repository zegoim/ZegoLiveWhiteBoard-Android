package com.zego.whiteboardedu.manager.utils;

import com.zego.whiteboardedu.manager.room.ZegoUserLiveStatus;

public class StringHelper {

    public static String getLiveStatusString(int liveStatus) {
        switch (liveStatus) {
            case ZegoUserLiveStatus.WAIT_CONNECT:
                return "待连接";
            case ZegoUserLiveStatus.CONNECTING:
                return "连接中";
            case ZegoUserLiveStatus.LIVE:
                return "已连接";
        }
        return "";
    }
}
