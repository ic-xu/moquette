package io.handler.codec.mqtt;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBufUtil;

/**
 * See <a href="https://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html#publish">MQTTV3.1/publish</a>
 */
public class MqttCustomerMessage extends MqttMessage  {

    public MqttCustomerMessage(
            MqttFixedHeader mqttFixedHeader, Object variableHeader,
            ByteBuf payload) {
        super(mqttFixedHeader, null, payload);
    }

    @Override
    public MqttPublishVariableHeader variableHeader() {
        return (MqttPublishVariableHeader) super.variableHeader();
    }

    @Override
    public ByteBuf payload() {
        return ByteBufUtil.ensureAccessible((ByteBuf) super.payload());
    }


}
