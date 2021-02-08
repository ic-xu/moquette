package io.moquette.persistence;

import io.moquette.broker.SessionRegistry;

import java.util.Queue;

public interface IQueueRepository {

    Queue<SessionRegistry.EnqueuedMessage> createQueue(String cli, boolean clean);
}
