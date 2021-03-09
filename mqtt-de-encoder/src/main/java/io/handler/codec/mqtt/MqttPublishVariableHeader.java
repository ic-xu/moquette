package io.handler.codec.mqtt;

import io.netty.util.internal.StringUtil;

/**
 * Variable Header of the {@link MqttPublishMessage}
 */
public final class MqttPublishVariableHeader {

    private final String topicName;
    private final int packetId;
    private final MqttProperties properties;

    public MqttPublishVariableHeader(String topicName, int packetId) {
        this(topicName, packetId, MqttProperties.NO_PROPERTIES);
    }

    public MqttPublishVariableHeader(String topicName, int packetId, MqttProperties properties) {
        this.topicName = topicName;
        this.packetId = packetId;
        this.properties = MqttProperties.withEmptyDefaults(properties);
    }

    public String topicName() {
        return topicName;
    }

    /**
     * @deprecated Use {@link #packetId()} instead.
     */
    @Deprecated
    public int messageId() {
        return packetId;
    }

    public int packetId() {
        return packetId;
    }

    public MqttProperties properties() {
        return properties;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("topicName=").append(topicName)
            .append(", packetId=").append(packetId)
            .append(']')
            .toString();
    }
}
