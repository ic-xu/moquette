package io.moquette.broker.subscriptions.plugin.imp;

import io.moquette.broker.subscriptions.Subscription;
import io.moquette.broker.subscriptions.Topic;
import io.moquette.utils.TopicUtils;
import io.moquette.broker.subscriptions.maptree.TopicTreeMap;
import io.moquette.broker.subscriptions.plugin.TopicsFilter;

import java.util.*;

public class IntersectionTopics implements TopicsFilter {


    @Override
    public boolean support(Topic topic) {
        String value = topic.getValue();
        return value.contains("&&") && !value.contains("||");
    }

    @Override
    public Set<Subscription> matchWithoutQosSharpening(TopicTreeMap rootTopicTreeMap,Topic topic) {
        String in_topics = TopicUtils.formatTopics(topic);
        String[] topicArr = in_topics.split("&&");
        ArrayList<Set<String>> subscriptions = new ArrayList<>(topicArr.length);
        Map<String, Subscription> resultMap = new HashMap<>();
        for (int i = 0; i < topicArr.length; i++) {
            Topic topic1 = new Topic(topicArr[i].trim());
            if (topic1.isValid()) {
                HashSet<String> clientId = new HashSet<>();
                for (Subscription sub : rootTopicTreeMap.getSubscriptions(TopicUtils.getTopicLevelArr(topic1.getValue()))) {
                    clientId.add(sub.getClientId());
                    if (i == 0) {
                        resultMap.put(sub.getClientId(), sub);
                    }
                }
                subscriptions.add(clientId);
            }
        }
        Set<String> resultSet = subscriptions.get(0);
        if (subscriptions.size() > 1) {
            for (int i = 1; i < subscriptions.size(); i++) {
                resultSet.retainAll(subscriptions.get(i));
            }
        }
        Set<Subscription> result = new HashSet<>();
        for (String clientId : resultSet) {
            result.add(resultMap.get(clientId));
        }
        return result;
    }
}
