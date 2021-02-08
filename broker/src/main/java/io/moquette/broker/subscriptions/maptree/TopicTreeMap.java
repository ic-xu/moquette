package io.moquette.broker.subscriptions.maptree;

import io.moquette.broker.subscriptions.Subscription;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TopicTreeMap {

    private String topicStringName;

    /**
     * 延迟初始化
     */
    private Set<Subscription> subscriptions;

    /**
     * 延迟初始化
     */
    private Map<String, TopicTreeMap> children;


    public Map<String, TopicTreeMap> getChildren() {
        return children;
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public TopicTreeMap(String topicStringName) {
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
            TopicTreeMap topicTreeMap = children.get(remove);
            if (topicTreeMap == null) {
                topicTreeMap = new TopicTreeMap(remove);
            }
            topicTreeMap.registerTopic(subscription, topicsRetain);
            children.put(remove, topicTreeMap);

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
            if(null==children){
                return;
            }
            TopicTreeMap topicTreeMap = children.get(topics[0]);
            if (topicTreeMap != null) {
                Set<Subscription> subscriptionsTmp = topicTreeMap.getSubscriptions();
                if (null != subscriptionsTmp) {
                    boolean remove = subscriptionsTmp.remove(subscription);
                    if (remove) {
                        TopicMapSubscriptionDirectory.size.decrementAndGet();
                    }
                    if (subscriptionsTmp.size() == 0) {
                        if (null == topicTreeMap.getChildren() || topicTreeMap.getChildren().size() == 0) {
                            topicTreeMap.cleanChildren();
                        }
                        topicTreeMap.cleanSubscriptions();
                    }
                }
                if (null == topicTreeMap.getChildren() && null == topicTreeMap.getSubscriptions()) {
                    children.remove(topics[0]);
                }
                if (null == children || children.size() == 0) {
                    children = null;
                }
            }

//            subscriptions.remove(subscription);
        } else {
            String topicRetain = topics[0];
            TopicTreeMap topicTreeMap = children.get(topicRetain);
            if (null != topicTreeMap) {
                String[] topicsRetain = Arrays.copyOfRange(topics, 1, topics.length);
                topicTreeMap.unRegisterTopic(subscription, topicsRetain);
                if(null== topicTreeMap.children && null == topicTreeMap.getSubscriptions()){
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
                TopicTreeMap topicTreeMap = children.get(topics[0].trim());
                if (null != topicTreeMap) {
                    Set<Subscription> subscriptions = topicTreeMap.getSubscriptions();
                    if (null != subscriptions)
                        subscriptionsResult.addAll(subscriptions);

                    if (null != topicTreeMap.getChildren()) {
                        /** 当前层级下的多层结构 */
                        TopicTreeMap multiLevelTopicTreeMap = topicTreeMap.getChildren().get("#");
                        if (null != multiLevelTopicTreeMap) {
                            Set<Subscription> multiLevelSubscription = multiLevelTopicTreeMap.getSubscriptions();
                            if (null != multiLevelSubscription)
                                subscriptionsResult.addAll(multiLevelSubscription);
                        }
                    }
                }

                /** 订阅当前层级的情况 + */
                TopicTreeMap currentLevel = children.get("+");
                if (null != currentLevel) {
                    Set<Subscription> subscriptions = currentLevel.getSubscriptions();
                    if (null != subscriptions)
                        subscriptionsResult.addAll(subscriptions);
                }

                /** 订阅当前层级下所有的情况 # */
                TopicTreeMap currentLevelAll = children.get("#");
                if (null != currentLevelAll) {
                    Set<Subscription> subscriptions = currentLevelAll.getSubscriptions();
                    if (null != subscriptions)
                        subscriptionsResult.addAll(subscriptions);
                }
            }
            return subscriptionsResult;
        }


        //订阅当前主题一下的情况 #
        TopicTreeMap currentTopicAll = children.get("#");
        if (null != currentTopicAll) {
            subscriptionsResult.addAll(currentTopicAll.getSubscriptions());
        }

        //剩下层次数组
        String[] topicsRetain = Arrays.copyOfRange(topics, 1, topics.length);

        //不包含通配符情况
        String topicString = topics[0];
        TopicTreeMap topicTreeMap = children.get(topicString);
        if (topicTreeMap != null) {
            subscriptionsResult.addAll(topicTreeMap.getSubscriptions(topicsRetain));
        }

        // 含有+ 通配符情况
        TopicTreeMap currentLevelTopicTreeMap = children.get("+");
        if (currentLevelTopicTreeMap != null) {
            subscriptionsResult.addAll(currentLevelTopicTreeMap.getSubscriptions(topicsRetain));
        }
        return subscriptionsResult;
    }
}
