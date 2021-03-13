package client.handler;

import client.process.MQTTConnectionProcess;
import client.process.MqttConnectionProcessFactory;
import io.handler.codec.mqtt.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;




public class MqttClientHandler extends SimpleChannelInboundHandler<Object> {
    private static final String ATTR_CONNECTION = "connection";
    private static final AttributeKey<Object> ATTR_KEY_CONNECTION = AttributeKey.valueOf(ATTR_CONNECTION);

    private MqttConnectionProcessFactory mqttConnectionProcessFactory;

    public MqttClientHandler(MqttConnectionProcessFactory mqttConnectionProcessFactory) {
        this.mqttConnectionProcessFactory = mqttConnectionProcessFactory;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 每次连接上来 使用连接工厂创建一个连接管理器
        MQTTConnectionProcess mqttConnectionProcess = mqttConnectionProcessFactory.createMqttConnectionProcess(ctx.channel());

        //把相应的连接对象封装在channel中，以供后续使用
        mqttConnection(ctx.channel(), mqttConnectionProcess);
    }

    private void mqttConnection(Channel channel, MQTTConnectionProcess mqttConnectionProcess) {
        channel.attr(ATTR_KEY_CONNECTION).set(mqttConnectionProcess);
    }


    private MQTTConnectionProcess mqttConnection(Channel channel) {
        return (MQTTConnectionProcess)channel.attr(ATTR_KEY_CONNECTION).get();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        mqttConnection(ctx.channel()).handleMessage((MqttMessage)msg);

    }
}
