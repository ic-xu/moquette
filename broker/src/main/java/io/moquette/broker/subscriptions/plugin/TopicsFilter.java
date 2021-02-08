package io.moquette.broker.subscriptions.plugin;

import io.moquette.broker.subscriptions.Subscription;
import io.moquette.broker.subscriptions.Topic;
import io.moquette.broker.subscriptions.maptree.TopicTreeMap;

import java.util.Set;


public interface TopicsFilter {

    boolean support(Topic topic);

    Set<Subscription> matchWithoutQosSharpening(TopicTreeMap rootTopicTreeMap, Topic topic);
}
