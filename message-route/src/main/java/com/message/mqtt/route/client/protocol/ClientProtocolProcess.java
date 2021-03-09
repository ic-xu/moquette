package com.message.mqtt.route.client.protocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.handler.codec.mqtt.*;

import java.util.Scanner;

/**
 * @author ben
 * @Title: basic
 * @Description:
 * B-borker; S-Subribler; P-Publisher
 **/

public class ClientProtocolProcess {



    /**
	 * B - S, B - P
	 * @param channel
	 * @param msg
	 */
	public void processConnectBack(Channel channel, MqttConnAckMessage msg) {
		MqttConnAckVariableHeader mqttConnAckVariableHeader = msg.variableHeader();
		String sErrorMsg = "";
		switch (mqttConnAckVariableHeader.connectReturnCode()) {
		case CONNECTION_ACCEPTED:
//			clientProcess.loginFinish(true, null);
			return;
		case CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD:
			sErrorMsg = "用户名密码错误";
			break;
		case CONNECTION_REFUSED_IDENTIFIER_REJECTED:
			sErrorMsg = "clientId不允许链接";
			break;
		case CONNECTION_REFUSED_SERVER_UNAVAILABLE:
			sErrorMsg = "服务不可用";
			break;
		case CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION:
			sErrorMsg = "mqtt 版本不可用";
			break;
		case CONNECTION_REFUSED_NOT_AUTHORIZED:
			sErrorMsg = "未授权登录";
			break;
		default:
			break;
		}

		channel.close();
	}

	/**
	 * B - P (Qos1)
	 * @param channel
	 * @param mqttMessage
	 */
	public void processPubAck(Channel channel, MqttMessage mqttMessage) {
		MqttMessageIdVariableHeader messageIdVariableHeader = (MqttMessageIdVariableHeader) mqttMessage
				.variableHeader();
		int messageId = messageIdVariableHeader.messageId();

	}


	/**
	 * B- P(Qos2)
	 * @param channel
	 * @param mqttMessage
	 */
	public void processPubRec(Channel channel, MqttMessage mqttMessage) {
		MqttMessageIdVariableHeader messageIdVariableHeader = (MqttMessageIdVariableHeader) mqttMessage
				.variableHeader();
		int messageId = messageIdVariableHeader.messageId();

	}

	/**
	 * B - P (Qos2)
	 * @param channel
	 * @param mqttMessage
	 */
	public void processPubComp(Channel channel, MqttMessage mqttMessage) {
		MqttMessageIdVariableHeader messageIdVariableHeader = (MqttMessageIdVariableHeader) mqttMessage
				.variableHeader();
		int messageId = messageIdVariableHeader.messageId();

	}

	/**
	 * B - S(Qos2)
	 * @param channel
	 * @param mqttMessage
	 */
	public void processPubRel(Channel channel, MqttMessage mqttMessage) {
		MqttMessageIdVariableHeader messageIdVariableHeader = (MqttMessageIdVariableHeader) mqttMessage
				.variableHeader();
		int messageId = messageIdVariableHeader.messageId();
	}


	/**
	 * B - S(Qos0, Qos1, Qos2)
	 * @param channel
	 * @param mqttMessage
	 */
	public void processPublish(Channel channel, MqttPublishMessage mqttMessage) {
		MqttFixedHeader mqttFixedHeader = mqttMessage.fixedHeader();
		MqttPublishVariableHeader mqttPublishVariableHeader = mqttMessage.variableHeader();
		ByteBuf payload = mqttMessage.payload();
		String topciName = mqttPublishVariableHeader.topicName();
		MqttQoS qosLevel = mqttFixedHeader.qosLevel();
		int messageId = mqttPublishVariableHeader.packetId();
	}

	/**
	 * B - P
	 * @param channel
	 * @param mqttMessage
	 */
	public void processSubAck(Channel channel, MqttSubAckMessage mqttMessage) {
		int messageId = mqttMessage.variableHeader().messageId();
//		this.consumerProcess.processSubAck(messageId);
	}

	/**
	 * B - S
	 * @param channel
	 * @param mqttMessage
	 */
	public void processUnSubBack(Channel channel, MqttMessage mqttMessage) {
		int messageId;
		if (mqttMessage instanceof MqttUnsubAckMessage) {
			MqttUnsubAckMessage mqttUnsubAckMessage = (MqttUnsubAckMessage) mqttMessage;
			messageId = mqttUnsubAckMessage.variableHeader().messageId();
		} else {
			MqttMessageIdVariableHeader o = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
			messageId = o.messageId();
		}
	}
}
