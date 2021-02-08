package io.moquette.broker.subscriptions.plugin;

import io.moquette.interception.messages.*;


public interface ConnectionFilter {

    void onConnect(InterceptConnectMessage msg);

    void onSubscribe(InterceptSubscribeMessage msg);

    void onUnsubscribe(InterceptUnsubscribeMessage msg);

    void onPublish(InterceptPublishMessage msg);

    void onPubRec(InterceptPublishMessage msg);

    void onPubComp(InterceptPublishMessage msg);

    void onPubRel(InterceptPublishMessage msg);

    void onPubAck(InterceptPublishMessage msg);

    void onDisconnect(InterceptDisconnectMessage msg);

    void onConnectionLost(InterceptConnectionLostMessage msg);

    void onMessageAcknowledged(InterceptAcknowledgedMessage msg);

}
