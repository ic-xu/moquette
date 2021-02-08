package io.moquette.persistence.memory;

import io.moquette.persistence.IQueueRepository;
import io.moquette.broker.SessionRegistry;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MemoryQueueRepository implements IQueueRepository {

    @Override
    public Queue<SessionRegistry.EnqueuedMessage> createQueue(String cli, boolean clean) {
        return new ConcurrentLinkedQueue<>();
    }
}
