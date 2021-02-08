package io.moquette.broker.subscriptions.maptree;

import io.moquette.broker.subscriptions.ISubscriptionsDirectory;
import io.moquette.broker.subscriptions.Subscription;
import io.moquette.broker.subscriptions.Topic;
import io.moquette.utils.TopicUtils;
import io.moquette.contants.ConstantsTopics;
import io.moquette.persistence.ISubscriptionsRepository;
import io.moquette.broker.subscriptions.plugin.imp.DefaultTopics;
import io.moquette.broker.subscriptions.plugin.imp.IntersectionTopics;
import io.moquette.broker.subscriptions.plugin.TopicsFilter;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("ALL")
public class TopicMapSubscriptionDirectory implements ISubscriptionsDirectory {


    private static final Logger LOG = LoggerFactory.getLogger(TopicMapSubscriptionDirectory.class);


    private TopicTreeMap rootTopicTreeMap;


    public static AtomicInteger size = new AtomicInteger(0);


    private volatile ISubscriptionsRepository subscriptionsRepository;


    private List<TopicsFilter> chains = new ArrayList<>();

    @Override
    public void init(ISubscriptionsRepository subscriptionsRepository) {
        LOG.info("Initializing CTrie");

        rootTopicTreeMap = new TopicTreeMap("ROOT");

        LOG.info("Initializing subscriptions store...");
        this.subscriptionsRepository = subscriptionsRepository;
        // reload any subscriptions persisted
        if (LOG.isTraceEnabled()) {
            LOG.trace("Reloading all stored subscriptions. SubscriptionTree = {}", dumpTree());
        }

        for (Subscription subscription : this.subscriptionsRepository.listAllSubscriptions()) {
            LOG.debug("Re-subscribing {}", subscription);

            rootTopicTreeMap.registerTopic(subscription, TopicUtils.getTopicLevelArr(subscription.topicFilter.getValue().trim()));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Stored subscriptions have been reloaded. SubscriptionTree = {}", dumpTree());
        }

        initChain();
    }


    /**
     * 添加责任链
     *
     * @param process
     * @return
     */
    public void initChain() {
        //默认主题
        chains.add(new DefaultTopics());

        //差交集主题
        chains.add(new IntersectionTopics());
    }

    /**
     * Given a topic string return the clients subscriptions that matches it. Topic string can't
     * contain character # and + because they are reserved to listeners subscriptions, and not topic
     * publishing.
     *
     * @param topic to use fo searching matching subscriptions.
     * @return the list of matching subscriptions, or empty if not matching.
     */

    public Set<Subscription> matchWithoutQosSharpening(Topic topic) {
        for (TopicsFilter chain : chains) {
            if (chain.support(topic))
                return chain.matchWithoutQosSharpening(rootTopicTreeMap, topic);
        }
        return new HashSet<>();
    }


//    @Override
//    public Set<Subscription> matchWithoutQosSharpening(Topic topic) {
//        String in_topics = topic.getValue().replace("in topics", "").trim().replace("'", "")
//            .replace("\"", "").replaceAll("\\s+", " ");
//
//        String[] topicArr = in_topics.split("&&");
//        if (topicArr.length == 1) {
//            return rootTopicTreeMap.getSubscriptions(TopicUtils.getTopicLevelArr(topicArr[0].trim()));
//        }
//        ArrayList<Set<String>> subscriptions = new ArrayList<>(topicArr.length);
//        Map<String, Subscription> resultMap = new HashMap<>();
//        for (int i = 0; i < topicArr.length; i++) {
//            Topic topic1 = new Topic(topicArr[i].trim());
//            if (topic1.isValid()) {
//                HashSet<String> clientId = new HashSet<>();
//                for (Subscription sub : rootTopicTreeMap.getSubscriptions(TopicUtils.getTopicLevelArr(topic1.getValue()))) {
//                    clientId.add(sub.clientId);
//                    if (i == 0) {
//                        resultMap.put(sub.clientId, sub);
//                    }
//                }
//                subscriptions.add(clientId);
//            }
//        }
//        Set<String> resultSet = subscriptions.get(0);
//        if (subscriptions.size() > 1) {
//            for (int i = 1; i < subscriptions.size(); i++) {
//                resultSet.retainAll(subscriptions.get(i));
//            }
//        }
//        Set<Subscription> result = new HashSet<>();
//        for (String clientId : resultSet) {
//            result.add(resultMap.get(clientId));
//        }
//        return result;
//    }

    @Override
    public Set<Subscription> matchQosSharpening(Topic topic, boolean isNeedBroadcasting) {
        final Set<Subscription> subscriptions = matchWithoutQosSharpening(topic);
        if (isNeedBroadcasting) {
            try {
                Set<Subscription> sys_broadcasting = rootTopicTreeMap.getChildren().get(ConstantsTopics.$SYS_BROADCASTING).getSubscriptions();
                int random = ThreadLocalRandom.current().nextInt(sys_broadcasting.size());
                Object[] objects = sys_broadcasting.toArray();
                Subscription broadcastSubscription = (Subscription) objects[random];
                subscriptions.add(broadcastSubscription);
            } catch (Exception ignore) {
            }
        }

        Map<String, Subscription> subsGroupedByClient = new HashMap<>();
        for (Subscription sub : subscriptions) {
            Subscription existingSub = subsGroupedByClient.get(sub.getClientId());
            // update the selected subscriptions if not present or if has a greater qos
            if (existingSub == null || existingSub.qosLessThan(sub)) {
                subsGroupedByClient.put(sub.getClientId(), sub);
            }
        }
        return new HashSet<>(subsGroupedByClient.values());
    }

    @Override
    public void add(Subscription newSubscription) {
        rootTopicTreeMap.registerTopic(newSubscription, TopicUtils.getTopicLevelArr(newSubscription.topicFilter.getValue().trim()));
        subscriptionsRepository.addNewSubscription(newSubscription);
    }

    /**
     * Removes subscription from CTrie, adds TNode when the last client unsubscribes, then calls for cleanTomb in a
     * separate atomic CAS operation.
     *
     * @param topic    the subscription's topic to remove.
     * @param clientID the Id of client owning the subscription.
     */
    @Override
    public void removeSubscription(Topic topic, String clientID) {
        rootTopicTreeMap.unRegisterTopic(new Subscription(clientID, topic, MqttQoS.AT_MOST_ONCE), TopicUtils.getTopicLevelArr(topic.getValue()));
        this.subscriptionsRepository.removeSubscription(topic.toString(), clientID);
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public String dumpTree() {
        return "";
    }

}
