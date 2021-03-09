package com.message.mqtt.route.client;

import com.message.mqtt.route.client.handler.MqttClientHandler;
import com.message.mqtt.route.client.protocol.ClientProtocolProcess;
import com.message.mqtt.route.client.protocol.ClientProtocolUtil;
import com.message.mqtt.route.client.protocol.MqttConnectOptions;
import com.message.mqtt.route.client.protocol.MqttProtocolUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.handler.codec.mqtt.MqttDecoder;
import io.handler.codec.mqtt.MqttEncoder;
import io.handler.codec.mqtt.MqttVersion;

import java.net.InetSocketAddress;
import java.util.Scanner;

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
//        for (int i = 0; i <10 ; i++) {
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setHost("10.92.33.61");
            mqttConnectOptions.setClientIdentifier("test-000000"+1);
            mqttConnectOptions.setUserName("admin");
            mqttConnectOptions.setPassword("passwd".getBytes());
            mqttConnectOptions.setHasWillFlag(false);
            mqttConnectOptions.setHasCleanSession(false);
            mqttConnectOptions.setHasUserName(true);
            mqttConnectOptions.setHasPassword(true);
            mqttConnectOptions.setMqttVersion(MqttVersion.MQTT_3_1);
            getInstance().doConnect(new Session(mqttConnectOptions));
//        }

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
        Scanner scanner = new Scanner(System.in);
        while (true){
            String s = scanner.nextLine();
            ByteBuf byteBuf1 = Unpooled.wrappedBuffer(s.getBytes());
            sync.channel().writeAndFlush(MqttProtocolUtil.customerMessage(false,1,false,byteBuf1));
        }
//        ChannelFuture channelFuture = sync.channel().closeFuture();
    }


}
