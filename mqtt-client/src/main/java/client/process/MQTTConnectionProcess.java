package client.process;

import client.MqttClient;
import io.handler.codec.mqtt.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import static io.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

/**
 * message handle core
 */
public final class MQTTConnectionProcess {


    private Channel channel;
    private volatile boolean connected;
    private final AtomicInteger lastPacketId = new AtomicInteger(0);

    public MQTTConnectionProcess(Channel channel) {
        this.channel = channel;
    }

    public void handleMessage(MqttMessage msg) {
        MqttMessageType messageType = msg.fixedHeader().messageType();
        switch (messageType) {
            case CUSTOMER:
                processCustomerMessage((MqttCustomerMessage)msg);
                break;
            case CONNECT:
                processConnect((MqttConnectMessage) msg);
                break;
                
            case CONNACK:
                processConnAck((MqttConnAckMessage)msg);
                break;
            case SUBSCRIBE:
                processSubscribe((MqttSubscribeMessage) msg);
                break;
            case UNSUBSCRIBE:
                processUnsubscribe((MqttUnsubscribeMessage) msg);
                break;
            case SUBACK:
                processSubAck((MqttSubAckMessage) msg);
                break;
            case UNSUBACK:
                processUnSubAck((MqttUnsubAckMessage)msg);
                break;
            case PUBLISH:
                processPublish((MqttPublishMessage) msg);
                break;
            case PUBACK:
                processPubAck(msg);
                break;
            case PUBREC:
                processPubRec(msg);
                break;
            case PUBCOMP:
                processPubComp(msg);
                break;
            case PUBREL:
                processPubRel(msg);
                break;
            case DISCONNECT:
                processDisconnect(msg);
                break;
            case PINGREQ:
                MqttFixedHeader pingHeader = new MqttFixedHeader(MqttMessageType.PINGRESP, false, AT_MOST_ONCE,
                    false, 0);
                MqttMessage pingResp = new MqttMessage(pingHeader);
                break;
            case PINGRESP:
            case AUTH:
            default:
                break;
        }
    }

    private void processCustomerMessage(MqttCustomerMessage msg) {
    }


    private void processConnect(MqttConnectMessage msg) {
    }


    private void processConnAck(MqttConnAckMessage msg) {
        MqttConnAckVariableHeader mqttConnAckVariableHeader = msg.variableHeader();
        String sErrorMsg = "";
        switch (mqttConnAckVariableHeader.connectReturnCode()) {
            case CONNECTION_ACCEPTED:
//			clientProcess.loginFinish(true, null);
                ByteBuf byteBuf1 = Unpooled.wrappedBuffer("ffffff".getBytes());
                DatagramPacket datagramPacket = new DatagramPacket(byteBuf1, new InetSocketAddress("34.249.122.178", 1883));
                MqttClient.getInstance().sendUDPMessage(datagramPacket);
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


    private void processSubscribe(MqttSubscribeMessage msg) {
    }

    private void processSubAck(MqttSubAckMessage msg) {
    }

    private void processUnSubAck(MqttUnsubAckMessage msg) {
    }


    private void processUnsubscribe(MqttUnsubscribeMessage msg) {
    }


    private void processPublish(MqttPublishMessage msg) {
    }


    private void processPubRec(MqttMessage msg) {
    }


    private void processPubComp(MqttMessage msg) {
    }


    private void processPubRel(MqttMessage msg) {
    }


    private void processDisconnect(MqttMessage msg) {
    }


    private void processPubAck(MqttMessage msg) {
    }


}
