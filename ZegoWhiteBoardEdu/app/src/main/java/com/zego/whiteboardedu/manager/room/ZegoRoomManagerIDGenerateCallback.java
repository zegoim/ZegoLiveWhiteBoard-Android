package com.zego.whiteboardedu.manager.room;

import com.zego.whiteboardedu.manager.entity.ZegoUser;

public interface ZegoRoomManagerIDGenerateCallback {

    String genLiveIDWithUser(ZegoUser user);
}
