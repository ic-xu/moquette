package io.moquette.broker;

import io.moquette.broker.config.BrokerConfiguration;
import io.moquette.broker.subscriptions.Topic;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class SessionTest {

    @Test
    public void testPubAckDrainMessagesRemainingInQueue() {
        final Queue<SessionRegistry.EnqueuedMessage> queuedMessages = new ConcurrentLinkedQueue<>();
        final Session client = new Session("Subscriber", true, null, queuedMessages,10);
        final EmbeddedChannel testChannel = new EmbeddedChannel();
        BrokerConfiguration brokerConfiguration = new BrokerConfiguration(true, false, false, false);
        MQTTConnection mqttConnection = new MQTTConnection(testChannel, brokerConfiguration, null, null, null);
        client.markConnecting();
        client.bind(mqttConnection);
        client.completeConnection();

        final Topic destinationTopic = new Topic("/a/b");
        sendQoS1To(client, destinationTopic, "Hello World!");
        // simulate a filling of inflight space and start pushing on queue
        for (int i = 0; i < 10; i++) {
            sendQoS1To(client, destinationTopic, "Hello World " + i + "!");
        }

        assertEquals("Inflight zone must be full, and the 11th message must be queued",
            1, queuedMessages.size());
        // Exercise
        client.pubAckReceived(1);

        // Verify
        assertTrue("Messages should be drained", queuedMessages.isEmpty());
    }

    private void sendQoS1To(Session client, Topic destinationTopic, String message) {
        final ByteBuf payload = ByteBufUtil.writeUtf8(UnpooledByteBufAllocator.DEFAULT, message);
        client.sendPublishOnSessionAtQos(destinationTopic, MqttQoS.AT_LEAST_ONCE, payload);
    }


    @Test
    public void testDelayQueue() throws InterruptedException {
        final DelayQueue<Session.InFlightPacket> inflightTimeouts = new DelayQueue<>();

        for (int i = 0; i < 30; i++) {
            inflightTimeouts.add(new Session.InFlightPacket(i, 5_000));
            System.out.println(inflightTimeouts.size());
        }

        for (Session.InFlightPacket f:inflightTimeouts) {
            if(f.packetId==10 ||f.packetId==20){
                inflightTimeouts.remove(f);
            }
        }
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        System.out.println(inflightTimeouts.size());

    }
}
