package com.message.mqtt.route.client;

import com.message.mqtt.route.client.handler.MqttClientHandler;
import com.message.mqtt.route.client.protocol.ClientProtocolProcess;
import com.message.mqtt.route.client.protocol.ClientProtocolUtil;
import com.message.mqtt.route.client.protocol.MqttConnectOptions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttVersion;

public class MqttClient {
    Bootstrap bootstrap = new Bootstrap();
    NioEventLoopGroup worker = new NioEventLoopGroup();

    private static volatile MqttClient instance;

    private MqttClient() {
        init();
    }

    public static MqttClient getInstance(){
        if(null==instance){
            synchronized (MqttClient.class){
                if(null == instance){
                    instance = new MqttClient();
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i <10 ; i++) {
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setHost("192.168.42.130");
            mqttConnectOptions.setClientIdentifier("test-000000"+i);
            mqttConnectOptions.setUserName("admin");
            mqttConnectOptions.setPassword("passwd".getBytes());
            mqttConnectOptions.setHasWillFlag(false);
            mqttConnectOptions.setHasCleanSession(false);
            mqttConnectOptions.setHasUserName(true);
            mqttConnectOptions.setHasPassword(true);
            mqttConnectOptions.setMqttVersion(MqttVersion.MQTT_3_1);
            getInstance().doConnect(new Session(mqttConnectOptions));
        }


    }


    void init() {
        bootstrap.group(worker)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, false)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100 * 1000)
            .handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) {
                    channel.pipeline().addLast("decoder", new MqttDecoder());
                    channel.pipeline().addLast("encoder", MqttEncoder.INSTANCE);
                    channel.pipeline().addLast("mqttHander", new MqttClientHandler(new ClientProtocolProcess()));
                }
            });
    }


    public void doConnect(Session session) throws InterruptedException {
        MqttConnectOptions mqttConnectOptions = session.getMqttConnectOptions();
        ChannelFuture sync = bootstrap.connect(mqttConnectOptions.getHost(), mqttConnectOptions.getPort()).sync();
        if(sync.isSuccess()){
            session.setChannel(sync.channel());
            sync.channel().writeAndFlush(ClientProtocolUtil.connectMessage(mqttConnectOptions));
        }
        ChannelFuture channelFuture = sync.channel().closeFuture();
    }


}
