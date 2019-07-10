package com.zego.whiteboardedu.entity;

public class RoomInfo {
    public String roomId;
    public String roomName;
    public long createAt;
    public String teacherId;
    public String replayUrl;

    public WhiteInfo whiteScreen;

    @Override
    public String toString() {
        return "RoomInfo{" +
                "roomId='" + roomId + '\'' +
                ", roomName='" + roomName + '\'' +
                ", createAt='" + createAt + '\'' +
                ", teacherId='" + teacherId + '\'' +
                ", replayUrl='" + replayUrl + '\'' +
                ", whiteScreen=" + whiteScreen +
                '}';
    }
}
