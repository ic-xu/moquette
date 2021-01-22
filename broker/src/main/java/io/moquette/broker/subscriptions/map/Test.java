package io.moquette.broker.subscriptions.map;

import io.moquette.broker.subscriptions.Subscription;
import io.moquette.broker.subscriptions.Topic;
import io.moquette.broker.subscriptions.TopicUtils;
import io.netty.handler.codec.mqtt.MqttQoS;

import java.util.Set;


public class Test {
    public static void main(String[] args) {

        TopicTreeMap topicTreeMap = new TopicTreeMap("root");

        register("+/+/b", topicTreeMap, new Subscription("1", new Topic("1"), MqttQoS.AT_MOST_ONCE));

        Set<Subscription> subscriptions = topicTreeMap.getSubscriptions(TopicUtils.getTopicLevelArr("sport/tennis/b"));
        System.err.println(subscriptions);
        topicTreeMap.unRegisterTopic(new Subscription("1",new Topic("1"),MqttQoS.AT_LEAST_ONCE),"#");
        System.err.println(topicTreeMap.toString());

//        topicTreeMap.unRegisterTopic(new Subscription("" + 0, new Topic(0 + ""), MqttQoS.AT_MOST_ONCE), "a/b/c/#".split("/"));
//        System.err.println(topicTreeMap.toString());
    }

    public static void register(String sss, TopicTreeMap topicTreeMap, Subscription subscription) {
        topicTreeMap.registerTopic(subscription, TopicUtils.getTopicLevelArr(sss));
    }
}
