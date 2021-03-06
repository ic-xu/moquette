package com.message.mqtt.route.client;

import com.message.mqtt.route.client.protocol.MqttConnectOptions;
import io.netty.channel.Channel;

public class Session {

    private MqttConnectOptions mqttConnectOptions;

    private Channel channel;

    public Session(MqttConnectOptions mqttConnectOptions) {
        this.mqttConnectOptions = mqttConnectOptions;
    }

    public MqttConnectOptions getMqttConnectOptions() {
        return mqttConnectOptions;
    }

    public void setMqttConnectOptions(MqttConnectOptions mqttConnectOptions) {
        this.mqttConnectOptions = mqttConnectOptions;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
