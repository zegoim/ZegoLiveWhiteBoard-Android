package com.zego.whiteboardedu.manager.room;

import android.support.annotation.NonNull;

import com.zego.whiteboardedu.manager.entity.ZegoUser;


/**
 * 房间信息
 */
public final class ZegoRoomInfo {

    /**
     * 房间ID
     */
    public String mRoomID;
    /**
     * 房间名字
     */
    public String mRoomName;
    /**
     * 房间拥有者
     */
    public ZegoUser mOwner;

    public ZegoRoomInfo(@NonNull String roomID, String roomName, ZegoUser owner) {
        mRoomID = roomID;
        mRoomName = roomName == null ? "" : roomName;
        mOwner = owner;
    }
}
