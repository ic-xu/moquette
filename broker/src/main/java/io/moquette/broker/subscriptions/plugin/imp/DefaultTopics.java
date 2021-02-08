package io.moquette.broker.subscriptions.plugin.imp;

import io.moquette.broker.subscriptions.Subscription;
import io.moquette.broker.subscriptions.Topic;
import io.moquette.utils.TopicUtils;
import io.moquette.broker.subscriptions.maptree.TopicTreeMap;
import io.moquette.broker.subscriptions.plugin.TopicsFilter;

import java.util.Set;

public class DefaultTopics implements TopicsFilter {


    @Override
    public boolean support(Topic topic) {
        String value = topic.getValue();
        return !value.contains("&&") && !value.contains("||");
    }

    @Override
    public Set<Subscription> matchWithoutQosSharpening(TopicTreeMap rootTopicTreeMap,Topic topic) {
        String in_topics = TopicUtils.formatTopics(topic);
        return rootTopicTreeMap.getSubscriptions(TopicUtils.getTopicLevelArr(in_topics));
    }
}
