package io.moquette.broker.handler;

import io.moquette.broker.MQTTConnection;
import io.moquette.broker.MQTTConnectionFactory;
import io.moquette.utils.NettyUtils;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.handler.codec.mqtt.MqttMessage;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;

/**
 * netty Acceptor Message Handler
 */
@Sharable
public class NewNettyMQTTHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NewNettyMQTTHandler.class);

    private static final String ATTR_CONNECTION = "connection";
    private static final AttributeKey<Object> ATTR_KEY_CONNECTION = AttributeKey.valueOf(ATTR_CONNECTION);

    private MQTTConnectionFactory connectionFactory;

    public NewNettyMQTTHandler(MQTTConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    private static void mqttConnection(Channel channel, MQTTConnection connection) {
        channel.attr(ATTR_KEY_CONNECTION).set(connection);
    }

    private static MQTTConnection mqttConnection(Channel channel) {
        return (MQTTConnection) channel.attr(ATTR_KEY_CONNECTION).get();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        MqttMessage msg = NettyUtils.validateMessage(message);
        //从channel 中获取相应的连接对象，通过连接对象处理相应的事务
        final MQTTConnection mqttConnection = mqttConnection(ctx.channel());
        try {
            mqttConnection.handleMessage(msg);
        } catch (Throwable ex) {
            //ctx.fireExceptionCaught(ex);
            LOG.error("Error processing protocol message: {}", msg.fixedHeader().messageType(), ex);
            ctx.channel().close().addListener((ChannelFutureListener) future -> LOG.info("Closed client channel due to exception in processing"));
        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        final MQTTConnection mqttConnection = mqttConnection(ctx.channel());
        mqttConnection.readCompleted();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 每次连接上来 使用连接工厂创建一个连接管理器
        MQTTConnection connection = connectionFactory.create(ctx.channel());
        //把相应的连接对象封装在channel中，以供后续使用
        mqttConnection(ctx.channel(), connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        final MQTTConnection mqttConnection = mqttConnection(ctx.channel());
        mqttConnection.handleConnectionLost();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("Unexpected exception while processing MQTT message. Closing Netty channel. CId={}",
                  NettyUtils.clientID(ctx.channel()), cause);
        ctx.close().addListener(CLOSE_ON_FAILURE);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
//        if (ctx.channel().isWritable()) {
//            m_processor.notifyChannelWritable(ctx.channel());
//        }
        final MQTTConnection mqttConnection = mqttConnection(ctx.channel());
        mqttConnection.writabilityChanged();
        ctx.fireChannelWritabilityChanged();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof InflightResenderHandler.ResendNotAckedPublishes) {
            final MQTTConnection mqttConnection = mqttConnection(ctx.channel());
            mqttConnection.resendNotAckedPublishes();
        }
        ctx.fireUserEventTriggered(evt);
    }

}
