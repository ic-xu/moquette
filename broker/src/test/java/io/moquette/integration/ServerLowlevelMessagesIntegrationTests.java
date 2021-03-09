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

package io.moquette.integration;

import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.testclient.Client;
import io.handler.codec.mqtt.*;
import io.handler.codec.mqtt.MqttMessage;
import org.eclipse.paho.client.mqttv3.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.awaitility.Awaitility;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import static io.handler.codec.mqtt.MqttConnectReturnCode.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

public class ServerLowlevelMessagesIntegrationTests {

    private static final Logger LOG = LoggerFactory.getLogger(ServerLowlevelMessagesIntegrationTests.class);
    static MqttClientPersistence s_dataStore;
    Server m_server;
    Client m_client;
    IMqttClient m_willSubscriber;
    MessageCollector m_messageCollector;
    IConfig m_config;
    MqttMessage receivedMsg;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    protected void startServer(String dbPath) throws IOException, InterruptedException {
        m_server = new Server();
        final Properties configProps = IntegrationUtils.prepareTestProperties(dbPath);
        m_config = new MemoryConfig(configProps);
        m_server.startServer(m_config);
    }

    @Before
    public void setUp() throws Exception {
        String dbPath = IntegrationUtils.tempH2Path(tempFolder);
        startServer(dbPath);
        m_client = new Client("localhost");
        m_willSubscriber = new MqttClient("tcp://localhost:1883", "Subscriber", s_dataStore);
        m_messageCollector = new MessageCollector();
        m_willSubscriber.setCallback(m_messageCollector);
    }

    @After
    public void tearDown() throws Exception {
        m_client.close();
        LOG.debug("After raw client close");
        Thread.sleep(300); // to let the close event pass before integration stop event
        m_server.stopServer();
        tempFolder.delete();
        LOG.debug("After asked integration to stop");
    }

    @Test
    public void elapseKeepAliveTime() {
        int keepAlive = 2; // secs

        MqttConnectMessage connectMessage = createConnectMessage("FAKECLNT", keepAlive);

        /*
         * ConnectMessage connectMessage = new ConnectMessage();
         * connectMessage.setProtocolVersion((byte) 3); connectMessage.setClientID("FAKECLNT");
         * connectMessage.setKeepAlive(keepAlive);
         */
        m_client.sendMessage(connectMessage);

        // wait 3 times the keepAlive
        Awaitility.await()
            .atMost(3 * keepAlive, TimeUnit.SECONDS)
            .until(m_client::isConnectionLost);
    }

    private static MqttConnectMessage createConnectMessage(String clientID, int keepAlive) {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_MOST_ONCE,
            false, 0);
        MqttConnectVariableHeader mqttConnectVariableHeader = new MqttConnectVariableHeader(
            MqttVersion.MQTT_3_1.protocolName(), MqttVersion.MQTT_3_1.protocolLevel(), false, false, false, 1, false,
            true, keepAlive);
        MqttConnectPayload mqttConnectPayload = new MqttConnectPayload(clientID, null, null,
                                                              null, (byte[]) null);
        return new MqttConnectMessage(mqttFixedHeader, mqttConnectVariableHeader, mqttConnectPayload);
    }

    @Test
    public void testWillMessageIsWiredOnClientKeepAliveExpiry() throws Exception {
        LOG.info("*** testWillMessageIsWiredOnClientKeepAliveExpiry ***");
        String willTestamentTopic = "/will/test";
        String willTestamentMsg = "Bye bye";

        m_willSubscriber.connect();
        m_willSubscriber.subscribe(willTestamentTopic, 0);

        m_client.clientId("FAKECLNT").connect(willTestamentTopic, willTestamentMsg);
        long connectTime = System.currentTimeMillis();

        Awaitility.await()
            .atMost(7, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                // but after the 2 KEEP ALIVE timeout expires it gets fired,
                // NB it's 1,5 * KEEP_ALIVE so 3 secs and some millis to propagate the message
                org.eclipse.paho.client.mqttv3.MqttMessage msg = m_messageCollector.getMessageImmediate();
                assertNotNull("the will message should be fired after keep alive!", msg);
                // the will message hasn't to be received before the elapsing of Keep Alive timeout
                assertTrue(System.currentTimeMillis() - connectTime > 3000);
                assertEquals(willTestamentMsg, new String(msg.getPayload(), UTF_8));
        });

        m_willSubscriber.disconnect();
    }

    @Test
    public void testRejectConnectWithEmptyClientID() throws InterruptedException {
        LOG.info("*** testRejectConnectWithEmptyClientID ***");
        m_client.clientId("").connect();

        this.receivedMsg = this.m_client.lastReceivedMessage();

        assertTrue(receivedMsg instanceof MqttConnAckMessage);
        MqttConnAckMessage connAck = (MqttConnAckMessage) receivedMsg;
        assertEquals(CONNECTION_REFUSED_IDENTIFIER_REJECTED, connAck.variableHeader().connectReturnCode());
    }

    @Test
    public void testWillMessageIsPublishedOnClientBadDisconnection() throws InterruptedException, MqttException {
        LOG.info("*** testWillMessageIsPublishedOnClientBadDisconnection ***");
        String willTestamentTopic = "/will/test";
        String willTestamentMsg = "Bye bye";
        m_willSubscriber.connect();
        m_willSubscriber.subscribe(willTestamentTopic, 0);
        m_client.clientId("FAKECLNT").connect(willTestamentTopic, willTestamentMsg);

        // kill will publisher
        m_client.close();

        // Verify will testament is published
        org.eclipse.paho.client.mqttv3.MqttMessage receivedTestament = m_messageCollector.waitMessage(1);
        assertEquals(willTestamentMsg, new String(receivedTestament.getPayload(), UTF_8));
        m_willSubscriber.disconnect();
    }

}
