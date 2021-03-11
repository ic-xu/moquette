package io.moquette.broker.handler.videotransfer.bean;

import io.moquette.broker.Session;

import java.util.concurrent.ConcurrentHashMap;

public class MemCons {


    // 在线用户表
    public static ConcurrentHashMap<String, Session> userBeans = new ConcurrentHashMap<>();

    // 在线房间表
    public static ConcurrentHashMap<String, RoomInfo> rooms = new ConcurrentHashMap<>();

}
