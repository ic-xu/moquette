package io.handler.codec.mqtt;

import io.netty.util.internal.StringUtil;

/**
 * Variable Header containing only Message Id
 * See <a href="https://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html#msg-id">MQTTV3.1/msg-id</a>
 */
public class MqttMessageIdVariableHeader {

    private final int messageId;

    public static MqttMessageIdVariableHeader from(int messageId) {
      if (messageId < 1 || messageId > 0xffff) {
        throw new IllegalArgumentException("messageId: " + messageId + " (expected: 1 ~ 65535)");
      }
      return new MqttMessageIdVariableHeader(messageId);
    }

    protected MqttMessageIdVariableHeader(int messageId) {
        this.messageId = messageId;
    }

    public int messageId() {
        return messageId;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("messageId=").append(messageId)
            .append(']')
            .toString();
    }

    public MqttMessageIdAndPropertiesVariableHeader withEmptyProperties() {
        return new MqttMessageIdAndPropertiesVariableHeader(messageId, MqttProperties.NO_PROPERTIES);
    }

    MqttMessageIdAndPropertiesVariableHeader withDefaultEmptyProperties() {
        return withEmptyProperties();
    }
}
