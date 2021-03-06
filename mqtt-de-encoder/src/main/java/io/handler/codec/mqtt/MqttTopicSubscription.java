package io.handler.codec.mqtt;

import io.netty.util.internal.StringUtil;

/**
 * Contains a topic name and Qos Level.
 * This is part of the {@link MqttSubscribePayload}
 */
public final class MqttTopicSubscription {

    private final String topicFilter;
    private final MqttSubscriptionOption option;

    public MqttTopicSubscription(String topicFilter, MqttQoS qualityOfService) {
        this.topicFilter = topicFilter;
        this.option = MqttSubscriptionOption.onlyFromQos(qualityOfService);
    }

    public MqttTopicSubscription(String topicFilter, MqttSubscriptionOption option) {
        this.topicFilter = topicFilter;
        this.option = option;
    }

    public String topicName() {
        return topicFilter;
    }

    public MqttQoS qualityOfService() {
        return option.qos();
    }

    public MqttSubscriptionOption option() {
        return option;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("topicFilter=").append(topicFilter)
            .append(", option=").append(this.option)
            .append(']')
            .toString();
    }
}
