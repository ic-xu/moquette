package com.message.mqtt.route.client;


import java.util.HashMap;
import java.util.Map;

public class SessionRegister {

    private MqttClient client;

    public SessionRegister(MqttClient client) {
        this.client = client;
    }

    private Map<String, Session> sessionPool = new HashMap<>();


    public synchronized void addOrupdateSession(Session session) throws InterruptedException {
        String key = session.getMqttConnectOptions().getHost() + session.getMqttConnectOptions().getPort();
        Session oldSession = sessionPool.get(key);
        if (null == oldSession) {
            Session putSession = sessionPool.put(key, session);
            if (null != putSession) {
                client.doConnect(putSession);
            }
        }

    }


    public synchronized void remove(String key) {
        Session remove = sessionPool.remove(key);
        if (null != remove && null != remove.getChannel()) {
            remove.getChannel().close();
        }
    }
}
