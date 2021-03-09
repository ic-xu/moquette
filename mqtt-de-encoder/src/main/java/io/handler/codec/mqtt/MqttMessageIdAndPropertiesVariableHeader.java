package io.handler.codec.mqtt;

import io.netty.util.internal.StringUtil;

/**
 * Variable Header containing, Packet Id and Properties as in MQTT v5 spec.
 */
public final class MqttMessageIdAndPropertiesVariableHeader extends MqttMessageIdVariableHeader {

    private final MqttProperties properties;

    public MqttMessageIdAndPropertiesVariableHeader(int messageId, MqttProperties properties) {
        super(messageId);
        if (messageId < 1 || messageId > 0xffff) {
            throw new IllegalArgumentException("messageId: " + messageId + " (expected: 1 ~ 65535)");
        }
        this.properties = MqttProperties.withEmptyDefaults(properties);
    }

    public MqttProperties properties() {
        return properties;
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + "[" +
                "messageId=" + messageId() +
                ", properties=" + properties +
                ']';
    }

    @Override
    MqttMessageIdAndPropertiesVariableHeader withDefaultEmptyProperties() {
        return this;
    }
}
