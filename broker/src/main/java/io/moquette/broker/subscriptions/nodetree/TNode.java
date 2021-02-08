package io.moquette.broker.subscriptions.nodetree;

import io.moquette.broker.subscriptions.Subscription;
import io.moquette.broker.subscriptions.Token;
import io.moquette.broker.subscriptions.nodetree.CNode;
import io.moquette.broker.subscriptions.nodetree.INode;

class TNode extends CNode {

    @Override
    INode childOf(Token token) {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    CNode copy() {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    public void add(INode newINode) {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    CNode addSubscription(Subscription newSubscription) {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    boolean containsOnly(String clientId) {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    public boolean contains(String clientId) {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    void removeSubscriptionsFor(String clientId) {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    boolean anyChildrenMatch(Token token) {
        return false;
    }
}
