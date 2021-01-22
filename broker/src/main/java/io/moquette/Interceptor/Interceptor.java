package io.moquette.Interceptor;

import io.moquette.interception.messages.*;


public interface Interceptor {

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
