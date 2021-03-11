package io.moquette.broker.handler.videotransfer.bean;


import io.moquette.broker.Session;

import java.util.concurrent.CopyOnWriteArrayList;

public class RoomInfo {
    // roomId
    private String roomId;
    // 创建人Id
    private String userId;
    // 房间里的人
    private CopyOnWriteArrayList<Session> Sessions = new CopyOnWriteArrayList<>();
    // 房间大小
    private int maxSize;
    // 现有人数
    private int currentSize;

    public RoomInfo() {
    }


    public CopyOnWriteArrayList<Session> getSessions() {
        return Sessions;
    }

    public void setSessions(CopyOnWriteArrayList<Session> userBeans) {
        this.Sessions = userBeans;
        setCurrentSize(this.Sessions.size());
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }
}
