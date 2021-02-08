package io.moquette.persistence.memory;

import io.moquette.persistence.ISubscriptionsRepository;
import io.moquette.broker.subscriptions.Subscription;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class MemorySubscriptionsRepository implements ISubscriptionsRepository {

    private Set<Subscription> subscriptions = new CopyOnWriteArraySet<>();

    @Override
    public List<Subscription> listAllSubscriptions() {
        return Collections.unmodifiableList(new ArrayList<>(subscriptions));
    }

    @Override
    public void addNewSubscription(Subscription subscription) {
        subscriptions.add(subscription);
    }

    @Override
    public void removeSubscription(String topic, String clientID) {
        subscriptions.stream()
            .filter(s -> s.getTopicFilter().toString().equals(topic) && s.getClientId().equals(clientID))
            .findFirst()
            .ifPresent(subscriptions::remove);
    }
}
