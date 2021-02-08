package io.moquette.broker.subscriptions.nodetree;

import io.moquette.broker.subscriptions.Subscription;
import io.moquette.broker.subscriptions.Token;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CNode {

    Token token;
    private Set<INode> children;
    Set<Subscription> subscriptions;


    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public CNode() {
        this.children = ConcurrentHashMap.newKeySet(1024*100);
        this.subscriptions = ConcurrentHashMap.newKeySet();
    }

    //Copy constructor
    private CNode(Token token, Set<INode> children, Set<Subscription> subscriptions) {
        this.token = token; // keep reference, root comparison in directory logic relies on it for now.
        this.subscriptions = ConcurrentHashMap.newKeySet();
        this.subscriptions.addAll(subscriptions);
//        new HashSet<>(subscriptions);
        this.children = ConcurrentHashMap.newKeySet(1024*100);
        this.children.addAll(children);
    }

    boolean anyChildrenMatch(Token token) {
        for (INode iNode : children) {
            final CNode child = iNode.mainNode();
            if (child.equalsToken(token)) {
                return true;
            }
        }
        return false;
    }

  public   List<INode> allChildren() {
       return new ArrayList<>(this.children);
//        return this.children;
    }

    INode childOf(Token token) {
        for (INode iNode : children) {
            final CNode child = iNode.mainNode();
            if (child.equalsToken(token)) {
                return iNode;
            }
        }
        throw new IllegalArgumentException("Asked for a token that doesn't exists in any child [" + token + "]");
    }

    private boolean equalsToken(Token token) {
        return token != null && this.token != null && this.token.equals(token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    CNode copy() {
        return new CNode(this.token, this.children, this.subscriptions);
    }

    public void add(INode newINode) {
        this.children.add(newINode);
    }
    public void remove(INode node) {
        this.children.remove(node);
    }

    CNode addSubscription(Subscription newSubscription) {
        // if already contains one with same topic and same client, keep that with higher QoS
        if (subscriptions.contains(newSubscription)) {
            final Subscription existing = subscriptions.stream()
                .filter(s -> s.equals(newSubscription))
                .findFirst().get();
            if (existing.getRequestedQos().value() < newSubscription.getRequestedQos().value()) {
                subscriptions.remove(existing);
                subscriptions.add(new Subscription(newSubscription));
            }
        } else {
            this.subscriptions.add(new Subscription(newSubscription));
        }
        return this;
    }

    /**
     * @return true iff the subscriptions contained in this node are owned by clientId
     *   AND at least one subscription is actually present for that clientId
     * */
    boolean containsOnly(String clientId) {
        for (Subscription sub : this.subscriptions) {
            if (!sub.getClientId().equals(clientId)) {
                return false;
            }
        }
        return !this.subscriptions.isEmpty();
    }

    //TODO this is equivalent to negate(containsOnly(clientId))
    public boolean contains(String clientId) {
        for (Subscription sub : this.subscriptions) {
            if (sub.getClientId().equals(clientId)) {
                return true;
            }
        }
        return false;
    }

    void removeSubscriptionsFor(String clientId) {
        Set<Subscription> toRemove = new HashSet<>();
        for (Subscription sub : this.subscriptions) {
            if (sub.getClientId().equals(clientId)) {
                toRemove.add(sub);
            }
        }
        this.subscriptions.removeAll(toRemove);
    }
}
