/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    https://www.eclipse.org/legal/epl-2.0
 * and the Eclipse Distribution License is available at 
 *   https://www.eclipse.org/org/documents/edl-v10.php
 *
 * Contributors:
 *    Dave Locke - initial API and implementation and/or initial documentation
 */
package io.client.mqttv3.internal.wire;

import io.client.mqttv3.MqttException;
import io.client.mqttv3.MqttMessage;
import io.client.mqttv3.MqttPersistenceException;

import java.io.*;

import static io.client.mqttv3.internal.wire.MqttPublish.encodePayload;

/**
 * An on-the-wire representation of an MQTT SEND message.
 */
public class MqttCustomerMessage extends MqttPersistableWireMessage {

    private MqttMessage message;

    private byte[] encodedPayload;


    private int qos;

    public void setQos(int qos) {
        if (qos >= 2) {
            this.qos = 2;
        } else if (qos <= 0) {
            this.qos = 0;
        }else {
            this.qos = qos;
        }

    }

    public int getQos() {
        return qos;
    }

    public MqttCustomerMessage(byte type, int qos, byte[] data) {
        super(type);
        setQos(qos);
        this.encodedPayload = data;
    }


    public void setMessageId(int msgId) {
        super.setMessageId(msgId);
        if (message instanceof MqttReceivedMessage) {
            ((MqttReceivedMessage)message).setMessageId(msgId);
        }
    }

    @Override
    protected byte getMessageInfo() {
        return 0;
    }

    @Override
    protected byte[] getVariableHeader() throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(buf);
        out.writeShort(msgId);
        return buf.toByteArray();
    }

    public boolean isMessageIdRequired() {
        return true;
    }


    @Override
    public int getPayloadLength() throws MqttPersistenceException {
        return encodedPayload.length;
    }

    public byte[] getPayload() throws MqttException {
        if (encodedPayload == null) {
            encodedPayload = encodePayload(message);
        }
        return encodedPayload;
    }

    @Override
    public int getHeaderLength() throws MqttPersistenceException {
        return super.getHeaderLength();
    }


//    public byte[] getHeader() throws MqttException {
//        try {
//            int first = ((getType() & 0x0f) << 4) ^ (getMessageInfo() & 0x0f);
//            byte[] varHeader = getVariableHeader();
//            int remLen = varHeader.length + getPayload().length;
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            DataOutputStream dos = new DataOutputStream(baos);
//            dos.writeByte(first);
//            dos.write(encodeMBI(remLen));
//            dos.write(varHeader);
//            dos.flush();
//            return baos.toByteArray();
//        } catch (IOException ioe) {
//            throw new MqttException(ioe);
//        }
//    }
}
