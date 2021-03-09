package io.moquette.broker;

import io.handler.codec.mqtt.MqttPublishMessage;
import io.moquette.broker.subscriptions.Subscription;
import io.moquette.broker.subscriptions.Topic;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.handler.codec.mqtt.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Session {

    private static final Logger LOG = LoggerFactory.getLogger(Session.class);
    private static final int FLIGHT_BEFORE_RESEND_MS = 5_000;
    private static int INFLIGHT_WINDOW_SIZE = 10;

    static class InFlightPacket implements Delayed {

        final int packetId;
        private long startTime;

        InFlightPacket(int packetId, long delayInMilliseconds) {
            this.packetId = packetId;
            this.startTime = System.currentTimeMillis() + delayInMilliseconds;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long diff = startTime - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            if ((this.startTime - ((InFlightPacket) o).startTime) == 0) {
                return 0;
            }
            if ((this.startTime - ((InFlightPacket) o).startTime) > 0) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    enum SessionStatus {
        CONNECTED, CONNECTING, DISCONNECTING, DISCONNECTED
    }

    static final class Will {

        final String topic;
        final ByteBuf payload;
        final MqttQoS qos;
        final boolean retained;

        Will(String topic, ByteBuf payload, MqttQoS qos, boolean retained) {
            this.topic = topic;
            this.payload = payload;
            this.qos = qos;
            this.retained = retained;
        }
    }

    private final String clientId;
    private boolean clean;
    private Will will;
    private Queue<SessionRegistry.EnqueuedMessage> sessionQueue;
    private final AtomicReference<SessionStatus> status = new AtomicReference<>(SessionStatus.DISCONNECTED);
    private MQTTConnection mqttConnection;

    //subscriptions 保存该用户订阅的主题信息，
    private Set<Subscription> subscriptions = new HashSet<>();
    private final Map<Integer, SessionRegistry.EnqueuedMessage> inflightWindow = new ConcurrentHashMap<>();
    private final DelayQueue<InFlightPacket> inflightTimeouts = new DelayQueue<>();
    private final Map<Integer, MqttPublishMessage> qos2Receiving = new ConcurrentHashMap<>();
    private final AtomicInteger inflightSlots = new AtomicInteger(INFLIGHT_WINDOW_SIZE); // this should be configurable

    public synchronized void addInflightWindow(Map<Integer, SessionRegistry.EnqueuedMessage> inflightWindows) {
        for (Integer packetId : inflightWindows.keySet()) {
            inflightTimeouts.add(new InFlightPacket(packetId, FLIGHT_BEFORE_RESEND_MS));
            inflightSlots.decrementAndGet();
        }
        inflightWindow.putAll(inflightWindows);
    }


    public Map<Integer, SessionRegistry.EnqueuedMessage> getInflightWindow() {
        return inflightWindow;
    }


    Session(String clientId, boolean clean, Will will, Queue<SessionRegistry.EnqueuedMessage> sessionQueue,Integer receiveMaximum) {
        this(clientId, clean, sessionQueue,receiveMaximum);
        this.will = will;
    }

    Session(String clientId, boolean clean, Queue<SessionRegistry.EnqueuedMessage> sessionQueue,Integer receiveMaximum) {
        this.clientId = clientId;
        this.clean = clean;
        this.INFLIGHT_WINDOW_SIZE = receiveMaximum;
        this.sessionQueue = sessionQueue;
    }

    void update(boolean clean, Will will) {
        this.clean = clean;
        this.will = will;
    }

    void markConnecting() {
        assignState(SessionStatus.DISCONNECTED, SessionStatus.CONNECTING);
    }

    boolean completeConnection() {
        return assignState(Session.SessionStatus.CONNECTING, Session.SessionStatus.CONNECTED);
    }

    void bind(MQTTConnection mqttConnection) {
        this.mqttConnection = mqttConnection;
    }

    public boolean disconnected() {
        return status.get() == SessionStatus.DISCONNECTED;
    }

    public boolean connected() {
        return status.get() == SessionStatus.CONNECTED;
    }

    public String getClientID() {
        return clientId;
    }

    public List<Subscription> getSubscriptions() {
        return new ArrayList<>(subscriptions);
    }

    public void addSubscriptions(List<Subscription> newSubscriptions) {
        subscriptions.addAll(newSubscriptions);
    }

    //这里应该要设置前缀匹配原则
    public void unSubscriptions(List<String> topic, String clientId) {

        ArrayList<Subscription> remove = new ArrayList<>();
        for (Subscription subscription : subscriptions) {
            if (topic.contains(subscription.getTopicFilter().getValue())) {
                remove.add(subscription);
            }
        }
        subscriptions.removeAll(remove);
    }

    public boolean hasWill() {
        return will != null;
    }

    public Will getWill() {
        return will;
    }

    boolean assignState(SessionStatus expected, SessionStatus newState) {
        return status.compareAndSet(expected, newState);
    }

    public void closeImmediately() {
        mqttConnection.dropConnection();
    }

    public void disconnect() {
        final boolean res = assignState(SessionStatus.CONNECTED, SessionStatus.DISCONNECTING);
        if (!res) {
            // someone already moved away from CONNECTED
            // TODO what to do?
            return;
        }

        mqttConnection = null;
        will = null;

        assignState(SessionStatus.DISCONNECTING, SessionStatus.DISCONNECTED);
    }

    boolean isClean() {
        return clean;
    }

    /**
     * @param packetId id
     */
    public void processPubRec(int packetId) {
        inflightWindow.remove(packetId);
        inflightSlots.incrementAndGet();
        if (canSkipQueue()) {
            inflightSlots.decrementAndGet();
            int pubRelPacketId = packetId/*mqttConnection.nextPacketId()*/;
            inflightWindow.put(pubRelPacketId, new SessionRegistry.PubRelMarker());
            inflightTimeouts.add(new InFlightPacket(pubRelPacketId, FLIGHT_BEFORE_RESEND_MS));
            MqttMessage pubRel = MQTTConnection.pubrel(pubRelPacketId);
            mqttConnection.sendIfWritableElseDrop(pubRel);

            drainQueueToConnection();
        } else {
            sessionQueue.add(new SessionRegistry.PubRelMarker());
        }
    }

    public void processPubComp(int messageID) {
        inflightWindow.remove(messageID);
        inflightSlots.incrementAndGet();

        drainQueueToConnection();

        // TODO notify the interceptor
//             final InterceptAcknowledgedMessage interceptAckMsg = new InterceptAcknowledgedMessage(inflightMsg,
// topic, username, messageID);
//                m_interceptor.notifyMessageAcknowledged(interceptAckMsg);
    }

    public void sendPublishOnSessionAtQos(Topic topic, MqttQoS qos, ByteBuf payload) {
        switch (qos) {
            case AT_MOST_ONCE:
                if (connected()) {
                    mqttConnection.sendPublishNotRetainedQos0(topic, qos, payload);
                }
                break;

            //这里发布的时候两个使用同样的处理方法即可
            case AT_LEAST_ONCE:
//                sendPublishQos1(topic, qos, payload);
//                break;
            case EXACTLY_ONCE:
//                sendPublishQos2(topic, qos, payload);
                sendPublish(topic, qos, payload);
                break;
            case FAILURE:
                LOG.error("Not admissible");
        }
    }

    private void sendPublishQos1(Topic topic, MqttQoS qos, ByteBuf payload) {
        if (!connected() && isClean()) {
            //pushing messages to disconnected not clean session
            return;
        }

        if (canSkipQueue()) {
            inflightSlots.decrementAndGet();
            int packetId = mqttConnection.nextPacketId();
            inflightWindow.put(packetId, new SessionRegistry.PublishedMessage(topic, qos, payload));
            inflightTimeouts.add(new InFlightPacket(packetId, FLIGHT_BEFORE_RESEND_MS));
            MqttPublishMessage publishMsg = MQTTConnection.notRetainedPublishWithMessageId(topic.toString(), qos,
                payload, packetId);
            mqttConnection.sendPublish(publishMsg);

            // TODO drainQueueToConnection();?
        } else {
            final SessionRegistry.PublishedMessage msg = new SessionRegistry.PublishedMessage(topic, qos, payload);
            sessionQueue.add(msg);
        }
    }

    private void sendPublishQos2(Topic topic, MqttQoS qos, ByteBuf payload) {
        if (canSkipQueue()) {
            inflightSlots.decrementAndGet();
            int packetId = mqttConnection.nextPacketId();
            ByteBuf byteBuf = Unpooled.copiedBuffer(payload);
            inflightWindow.put(packetId, new SessionRegistry.PublishedMessage(topic, qos, byteBuf));
            inflightTimeouts.add(new InFlightPacket(packetId, FLIGHT_BEFORE_RESEND_MS));
            MqttPublishMessage publishMsg = MQTTConnection.notRetainedPublishWithMessageId(topic.toString(), qos,
                payload, packetId);
            mqttConnection.sendPublish(publishMsg);

            drainQueueToConnection();
        } else {
            final SessionRegistry.PublishedMessage msg = new SessionRegistry.PublishedMessage(topic, qos, payload);
            sessionQueue.add(msg);
        }
    }


    private void sendPublish(Topic topic, MqttQoS qos, ByteBuf payload) {
        final SessionRegistry.PublishedMessage msg = new SessionRegistry.PublishedMessage(topic, qos, payload);
        sessionQueue.add(msg);
        drainQueueToConnection();
    }

    private boolean canSkipQueue() {
        return sessionQueue.isEmpty() &&
            inflightSlots.get() > 0 &&
            connected() &&
            mqttConnection.channel.isWritable();
    }

    private boolean inflighHasSlotsAndConnectionIsUp() {
        return inflightSlots.get() > 0 &&
            connected() &&
            mqttConnection.channel.isWritable();
    }

    void pubAckReceived(int ackPacketId) {
        // TODO remain to invoke in somehow m_interceptor.notifyMessageAcknowledged
        inflightWindow.remove(ackPacketId);
        inflightSlots.incrementAndGet();
        for (InFlightPacket next : inflightTimeouts) {
            if (next.packetId == ackPacketId) {
                inflightTimeouts.remove(next);
                break;
            }
        }
        drainQueueToConnection();
    }

    public void flushAllQueuedMessages() {
        drainQueueToConnection();
    }

//    public void resendInflightNotAcked() {
//        if (inflightTimeouts.size() == 0) {
//            for (Integer msgId : inflightWindow.keySet()) {
//                SessionRegistry.EnqueuedMessage enqueuedMessage = inflightWindow.get(msgId);
//                if (enqueuedMessage instanceof SessionRegistry.PublishedMessage) {
//                    SessionRegistry.EnqueuedMessage remove = inflightWindow.remove(msgId);
//                    SessionRegistry.PublishedMessage message = (SessionRegistry.PublishedMessage) remove;
//                    sendPublishOnSessionAtQos(message.topic, message.publishingQos, message.payload);
//                }
//            }
//        } else {
//            Collection<InFlightPacket> expired = new ArrayList<>(INFLIGHT_WINDOW_SIZE);
//            inflightTimeouts.drainTo(expired);
//            debugLogPacketIds(expired);
//            for (InFlightPacket notAckPacketId : expired) {
//                if (inflightWindow.containsKey(notAckPacketId.packetId)) {
//                    final SessionRegistry.PublishedMessage msg =
//                        (SessionRegistry.PublishedMessage) inflightWindow.get(notAckPacketId.packetId);
//                    final Topic topic = msg.topic;
//                    final MqttQoS qos = msg.publishingQos;
//                    final ByteBuf payload = msg.payload;
//                    final ByteBuf copiedPayload = payload.retainedDuplicate();
////                ByteBuf byteBuf = Unpooled.copiedBuffer(payload);
//                    MqttPublishMessage publishMsg = publishNotRetainedDuplicated(notAckPacketId, topic, qos, copiedPayload);
//                    mqttConnection.sendPublish(publishMsg);
//                }
//            }
//        }
//    }

    public void resendInflightNotAcked() {
        InFlightPacket notAckPacketId = inflightTimeouts.peek();
        if (notAckPacketId != null) {
            if (inflightWindow.containsKey(notAckPacketId.packetId)) {
                final SessionRegistry.PublishedMessage msg =
                    (SessionRegistry.PublishedMessage) inflightWindow.get(notAckPacketId.packetId);
                final Topic topic = msg.topic;
                final MqttQoS qos = msg.publishingQos;
                final ByteBuf payload = msg.payload;
                final ByteBuf copiedPayload = payload.retainedDuplicate();
                MqttPublishMessage publishMsg = publishNotRetainedDuplicated(notAckPacketId, topic, qos, copiedPayload);
                mqttConnection.sendPublish(publishMsg);
            }
        }


//            while (notAckPacketId != null) {
//                if (inflightWindow.containsKey(notAckPacketId.packetId)) {
//                    final SessionRegistry.PublishedMessage msg =
//                        (SessionRegistry.PublishedMessage) inflightWindow.get(notAckPacketId.packetId);
//                    sessionQueue.add(msg);
//                    notAckPacketId = inflightTimeouts.poll();
//                }
//            }
    }


    private void debugLogPacketIds(Collection<InFlightPacket> expired) {
        if (!LOG.isDebugEnabled() || expired.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (InFlightPacket packet : expired) {
            sb.append(packet.packetId).append(", ");
        }
        LOG.debug("Resending {} in flight packets [{}]", expired.size(), sb);
    }

    private MqttPublishMessage publishNotRetainedDuplicated(InFlightPacket notAckPacketId, Topic topic, MqttQoS
        qos,
                                                            ByteBuf payload) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, true, qos, false, 0);
        MqttPublishVariableHeader varHeader = new MqttPublishVariableHeader(topic.toString(), notAckPacketId.packetId);
        return new MqttPublishMessage(fixedHeader, varHeader, payload);
    }

    private void drainQueueToConnection() {
        resendInflightNotAcked();
        // consume the queue
        while (!sessionQueue.isEmpty() && inflighHasSlotsAndConnectionIsUp()) {
            final SessionRegistry.EnqueuedMessage msg = sessionQueue.remove();
            inflightSlots.decrementAndGet();
            int sendPacketId = mqttConnection.nextPacketId();
            inflightWindow.put(sendPacketId, msg);
            if (msg instanceof SessionRegistry.PubRelMarker) {
                MqttMessage pubRel = MQTTConnection.pubrel(sendPacketId);
                mqttConnection.sendIfWritableElseDrop(pubRel);
            } else {
                final SessionRegistry.PublishedMessage msgPub = (SessionRegistry.PublishedMessage) msg;
                MqttPublishMessage publishMsg = MQTTConnection.notRetainedPublishWithMessageId(msgPub.topic.toString(),
                    msgPub.publishingQos,
                    msgPub.payload.retain(), sendPacketId);
                mqttConnection.sendPublish(publishMsg);
            }
        }
    }

    public void writabilityChanged() {
        drainQueueToConnection();
    }

    public void sendQueuedMessagesWhileOffline() {
        LOG.trace("Republishing all saved messages for session {} on CId={}", this, this.clientId);
        drainQueueToConnection();
    }

    void sendRetainedPublishOnSessionAtQos(Topic topic, MqttQoS qos, ByteBuf payload) {
        if (qos != MqttQoS.AT_MOST_ONCE) {
            // QoS 1 or 2
            mqttConnection.sendPublishRetainedWithPacketId(topic, qos, payload);
        } else {
            mqttConnection.sendPublishRetainedQos0(topic, qos, payload);
        }
    }

    public void receivedPublishQos2(int messageID, MqttPublishMessage msg) {
        qos2Receiving.put(messageID, msg);
        msg.retain(); // retain to put in the inflight maptree
        mqttConnection.sendPublishReceived(messageID);
    }

    public void receivedPubRelQos2(int messageID) {
        final MqttPublishMessage removedMsg = qos2Receiving.remove(messageID);
        ReferenceCountUtil.release(removedMsg);
    }

    Optional<InetSocketAddress> remoteAddress() {
        if (connected()) {
            return Optional.of(mqttConnection.remoteAddress());
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "Session{" +
            "clientId='" + clientId + '\'' +
            ", clean=" + clean +
            ", status=" + status +
            ", inflightSlots=" + inflightSlots +
            '}';
    }
}
