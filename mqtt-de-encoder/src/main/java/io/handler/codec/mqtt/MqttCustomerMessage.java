package io.handler.codec.mqtt;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.internal.StringUtil;

/**
 * See <a href="https://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html#publish">MQTTV3.1/publish</a>
 */
public class MqttCustomerMessage extends MqttMessage  {

    public MqttCustomerMessage(
            MqttFixedHeader mqttFixedHeader, MqttCustomerVariableHeader variableHeader,
            ByteBuf payload) {
        super(mqttFixedHeader, variableHeader, payload);
    }

    @Override
    public MqttCustomerVariableHeader variableHeader() {
        return (MqttCustomerVariableHeader) super.variableHeader();
    }

    @Override
    public ByteBuf payload() {
        return ByteBufUtil.ensureAccessible((ByteBuf) super.payload());
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("fixedHeader=").append(fixedHeader() != null ? fixedHeader().toString() : "")
            .append(", variableHeader=").append(variableHeader() != null ? variableHeader().toString() : "")
            .append(']')
            .toString();
    }
}
