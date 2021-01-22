package io.moquette.broker.subscriptions.map;

import io.moquette.broker.subscriptions.Subscription;
import io.moquette.broker.subscriptions.TopicMapSubscriptionDirectory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TopicMap {

    private String topicStringName;

    /**
     * 延迟初始化
     */
    private Set<Subscription> subscriptions;

    /**
     * 延迟初始化
     */
    private Map<String, TopicMap> children;


    public Map<String, TopicMap> getChildren() {
        return children;
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public TopicMap(String topicStringName) {
        this.topicStringName = topicStringName;
    }

    public String getTopicStringName() {
        return topicStringName;
    }

    public void cleanSubscriptions() {
        if (subscriptions != null) {
            this.subscriptions.clear();
            this.subscriptions = null;
        }
    }

    public void cleanChildren() {
        if (this.children != null) {
            this.children.clear();
            this.children = null;
        }
    }

    public void registerTopic(Subscription subscription, String... topics) {
        if (topics.length < 1) {
            if (null == subscriptions)
                subscriptions = ConcurrentHashMap.newKeySet();
            boolean add = subscriptions.add(subscription);
            if (add) {
                TopicMapSubscriptionDirectory.size.incrementAndGet();
            }
        } else {
            String remove = topics[0].trim();
            String[] topicsRetain = Arrays.copyOfRange(topics, 1, topics.length);
            if (null == children) {
                children = new ConcurrentHashMap<>();
            }
            TopicMap topicMap = children.get(remove);
            if (topicMap == null) {
                topicMap = new TopicMap(remove);
            }
            topicMap.registerTopic(subscription, topicsRetain);
            children.put(remove, topicMap);

        }
    }

    /**
     * unRegisterTopic topics
     *
     * @param subscription s
     * @param topics       topic
     */
    public void unRegisterTopic(Subscription subscription, String... topics) {

        if (topics.length < 1) {
        } else if (topics.length == 1) {
            TopicMap topicMap = children.get(topics[0]);
            if (topicMap != null) {
                Set<Subscription> subscriptionsTmp = topicMap.getSubscriptions();
                if (null != subscriptionsTmp) {
                    boolean remove = subscriptionsTmp.remove(subscription);
                    if (remove) {
                        TopicMapSubscriptionDirectory.size.decrementAndGet();
                    }
                    if (subscriptionsTmp.size() == 0) {
                        if (null == topicMap.getChildren() || topicMap.getChildren().size() == 0) {
                            topicMap.cleanChildren();
                        }
                        topicMap.cleanSubscriptions();
                    }
                }
                if (null == topicMap.getChildren() && null == topicMap.getSubscriptions()) {
                    children.remove(topics[0]);
                }
                if (null == children || children.size() == 0) {
                    children = null;
                }
            }

//            subscriptions.remove(subscription);
        } else {
            String topicRetain = topics[0];
            TopicMap topicMap = children.get(topicRetain);
            if (null != topicMap) {
                String[] topicsRetain = Arrays.copyOfRange(topics, 1, topics.length);
                topicMap.unRegisterTopic(subscription, topicsRetain);
                if(null==topicMap.children && null == topicMap.getSubscriptions()){
                    children.remove(topicRetain);
                }
            }
        }
    }


    public Set<Subscription> getSubscriptions(String... topics) {
        Set<Subscription> subscriptionsResult = new HashSet<>();
        //表示最后一层
        if (topics.length <= 1) {
            if (children != null) {
                /** 精确匹配的情况 */
                TopicMap topicMap = children.get(topics[0].trim());
                if (null != topicMap) {
                    Set<Subscription> subscriptions = topicMap.getSubscriptions();
                    if (null != subscriptions)
                        subscriptionsResult.addAll(subscriptions);

                    if (null != topicMap.getChildren()) {
                        /** 当前层级下的多层结构 */
                        TopicMap multiLevelTopicMap = topicMap.getChildren().get("#");
                        if (null != multiLevelTopicMap) {
                            Set<Subscription> multiLevelSubscription = multiLevelTopicMap.getSubscriptions();
                            if (null != multiLevelSubscription)
                                subscriptionsResult.addAll(multiLevelSubscription);
                        }
                    }
                }

                /** 订阅当前层级的情况 + */
                TopicMap currentLevel = children.get("+");
                if (null != currentLevel) {
                    Set<Subscription> subscriptions = currentLevel.getSubscriptions();
                    if (null != subscriptions)
                        subscriptionsResult.addAll(subscriptions);
                }

                /** 订阅当前层级下所有的情况 # */
                TopicMap currentLevelAll = children.get("#");
                if (null != currentLevelAll) {
                    Set<Subscription> subscriptions = currentLevelAll.getSubscriptions();
                    if (null != subscriptions)
                        subscriptionsResult.addAll(subscriptions);
                }
            }
            return subscriptionsResult;
        }


        //订阅当前主题一下的情况 #
        TopicMap currentTopicAll = children.get("#");
        if (null != currentTopicAll) {
            subscriptionsResult.addAll(currentTopicAll.getSubscriptions());
        }

        //剩下层次数组
        String[] topicsRetain = Arrays.copyOfRange(topics, 1, topics.length);

        //不包含通配符情况
        String topicString = topics[0];
        TopicMap topicMap = children.get(topicString);
        if (topicMap != null) {
            subscriptionsResult.addAll(topicMap.getSubscriptions(topicsRetain));
        }

        // 含有+ 通配符情况
        TopicMap CurrentLevelTopicMap = children.get("+");
        if (CurrentLevelTopicMap != null) {
            subscriptionsResult.addAll(CurrentLevelTopicMap.getSubscriptions(topicsRetain));
        }
        return subscriptionsResult;
    }
}
