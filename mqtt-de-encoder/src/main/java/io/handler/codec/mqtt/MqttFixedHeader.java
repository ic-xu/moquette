package io.handler.codec.mqtt;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

/**
 * See <a href="https://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html#fixed-header">
 *     MQTTV3.1/fixed-header</a>
 */
public final class MqttFixedHeader {

    private final MqttMessageType messageType;
    private final boolean isDup;
    private final MqttQoS qosLevel;
    private final boolean isRetain;
    private final int remainingLength;

    public MqttFixedHeader(
            MqttMessageType messageType,
            boolean isDup,
            MqttQoS qosLevel,
            boolean isRetain,
            int remainingLength) {
        this.messageType = ObjectUtil.checkNotNull(messageType, "messageType");
        this.isDup = isDup;
        this.qosLevel = ObjectUtil.checkNotNull(qosLevel, "qosLevel");
        this.isRetain = isRetain;
        this.remainingLength = remainingLength;
    }

    public MqttMessageType messageType() {
        return messageType;
    }

    public boolean isDup() {
        return isDup;
    }

    public MqttQoS qosLevel() {
        return qosLevel;
    }

    public boolean isRetain() {
        return isRetain;
    }

    public int remainingLength() {
        return remainingLength;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("messageType=").append(messageType)
            .append(", isDup=").append(isDup)
            .append(", qosLevel=").append(qosLevel)
            .append(", isRetain=").append(isRetain)
            .append(", remainingLength=").append(remainingLength)
            .append(']')
            .toString();
    }
}
