/*
 * Copyright (c) 2012-2018 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.moquette.broker.subscriptions;

import io.moquette.broker.ISubscriptionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@SuppressWarnings("ALL")
public class CTrieSubscriptionDirectory implements ISubscriptionsDirectory {

    private static final Logger LOG = LoggerFactory.getLogger(CTrieSubscriptionDirectory.class);

    private CTrie ctrie;

    private volatile ISubscriptionsRepository subscriptionsRepository;

    @Override
    public void init(ISubscriptionsRepository subscriptionsRepository) {
        LOG.info("Initializing CTrie");
//        ctrie = new CTrie();

        LOG.info("Initializing subscriptions store...");
        this.subscriptionsRepository = subscriptionsRepository;
        // reload any subscriptions persisted
        if (LOG.isTraceEnabled()) {
            LOG.trace("Reloading all stored subscriptions. SubscriptionTree = {}", dumpTree());
        }

        for (Subscription subscription : this.subscriptionsRepository.listAllSubscriptions()) {
            LOG.debug("Re-subscribing {}", subscription);
            ctrie.addToTree(subscription);

        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Stored subscriptions have been reloaded. SubscriptionTree = {}", dumpTree());
        }
    }

    Optional<CNode> lookup(Topic topic) {
        return ctrie.lookup(topic);
    }

    /**
     * Given a topic string return the clients subscriptions that matches it. Topic string can't
     * contain character # and + because they are reserved to listeners subscriptions, and not topic
     * publishing.
     *
     * @param topic to use fo searching matching subscriptions.
     * @return the list of matching subscriptions, or empty if not matching.
     */
    @Override
    public Set<Subscription> matchWithoutQosSharpening(Topic topic) {
        String in_topics = topic.getValue().replace("in topics", "").trim().replace("'", "").replace("\"", "").replaceAll(" +"," ");;
        String[] topicArr = in_topics.split("&&");
        if(topicArr.length==1){
            return ctrie.recursiveMatch(topic);
        }
        ArrayList<Set<String>> subscriptions = new ArrayList<>(topicArr.length);
        Map<String, Subscription> resultMap = new HashMap<>();
        for (int i = 0; i < topicArr.length; i++) {
            Topic topic1 = new Topic(topicArr[i].trim());
            if (topic1.isValid()) {
                HashSet<String> clientId = new HashSet<>();
                for (Subscription sub : ctrie.recursiveMatch(topic1)) {
                    clientId.add(sub.clientId);
                    if (i == 0) {
                        resultMap.put(sub.clientId, sub);
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
        for (String clientId:resultSet) {
            result.add(resultMap.get(clientId));
        }
        return result;
//        return ctrie.recursiveMatch(topic);
    }


    @Override
    public Set<Subscription> matchQosSharpening(Topic topic,boolean isNeedBroadcasting) {
        final Set<Subscription> subscriptions = matchWithoutQosSharpening(topic);

        Map<String, Subscription> subsGroupedByClient = new HashMap<>();
        for (Subscription sub : subscriptions) {
            Subscription existingSub = subsGroupedByClient.get(sub.clientId);
            // update the selected subscriptions if not present or if has a greater qos
            if (existingSub == null || existingSub.qosLessThan(sub)) {
                subsGroupedByClient.put(sub.clientId, sub);
            }
        }
        return new HashSet<>(subsGroupedByClient.values());
    }

    @Override
    public void add(Subscription newSubscription) {
        ctrie.addToTree(newSubscription);
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
        ctrie.removeFromTree(topic, clientID);
        this.subscriptionsRepository.removeSubscription(topic.toString(), clientID);
    }

    @Override
    public int size() {
        return ctrie.size();
    }

    @Override
    public String dumpTree() {
        return ctrie.dumpTree();
    }
}
