package com.message.mqtt.route.client.handler;

import com.message.mqtt.route.client.protocol.ClientProtocolProcess;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author ben
 * @Title: basic
 * @Description:
 **/

public class MqttClientHandler extends SimpleChannelInboundHandler<Object> {
    private ClientProtocolProcess clientProtocolProcess;
    //实例化一个group保存客户端连接
    public static ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup("ChannelGroups", GlobalEventExecutor.INSTANCE);

    public MqttClientHandler(ClientProtocolProcess clientProtocolProcess) {
        this.clientProtocolProcess = clientProtocolProcess;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msgx) throws Exception {
        CHANNEL_GROUP.add(ctx.channel());
        System.out.println(CHANNEL_GROUP.size());
        if (msgx == null) {return ;}
        MqttMessage msg = (MqttMessage) msgx;
//        NettyLog.debug("read: {}", msg.fixedHeader().messageType());
        MqttFixedHeader mqttFixedHeader = msg.fixedHeader();
        switch (mqttFixedHeader.messageType()) {
            case CONNACK:
                clientProtocolProcess.processConnectBack(ctx.channel(), (MqttConnAckMessage) msg);
                break;
            case UNSUBACK:
                clientProtocolProcess.processUnSubBack(ctx.channel(), msg);
                break;
            case PUBLISH:
                clientProtocolProcess.processPublish(ctx.channel(), (MqttPublishMessage) msg);
                break;
            case PUBACK:
                clientProtocolProcess.processPubAck(ctx.channel(), msg);
                break;
            case PUBREC:
                clientProtocolProcess.processPubRec(ctx.channel(), msg);
                break;
            case PUBREL:
                clientProtocolProcess.processPubRel(ctx.channel(), msg);
                break;
            case PUBCOMP:
                clientProtocolProcess.processPubComp(ctx.channel(), msg);
                break;
            case SUBACK:
                clientProtocolProcess.processSubAck(ctx.channel(), (MqttSubAckMessage) msg);
                break;
            default:
                break;
        }
    }
}
