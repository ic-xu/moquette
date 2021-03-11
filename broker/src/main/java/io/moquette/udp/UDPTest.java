package io.moquette.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class UDPTest {

    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup work = new NioEventLoopGroup();

        Channel channel = new Bootstrap()
            .group(work)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel nioDatagramChannel) {
                    nioDatagramChannel.pipeline()
                        .addLast(new SimpleChannelInboundHandler<DatagramPacket>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
                                datagramPacket.retain();
                                byte[] buf = new byte[datagramPacket.content().readableBytes()];
                                datagramPacket.content().readBytes(buf);
                                System. out.println(new String(buf));
                            }
                        })
                    ;
                }
            }).bind(0).sync().channel();
//        long start = System.currentTimeMillis();
//        ByteBuf byteBuf = Unpooled.directBuffer(1024);
//        byte[] bytes = ("test" +1).getBytes();
//        byteBuf.writeBytes(bytes);
//        for (int i = 0; i < 10000000; i++) {
//            byteBuf.retain();
//            DatagramPacket datagramPacket = new DatagramPacket(byteBuf, new InetSocketAddress("127.0.0.1", 1883));
//            channel.writeAndFlush(datagramPacket);
//        }
//
//        System.err.println("************************"+(System.currentTimeMillis()-start));
        Scanner scanner = new Scanner(System.in);
        while (true){
            String s = scanner.nextLine();
            ByteBuf byteBuf1 = Unpooled.wrappedBuffer(s.getBytes());
            DatagramPacket datagramPacket = new DatagramPacket(byteBuf1, new InetSocketAddress("34.249.122.178", 1883));
            channel.writeAndFlush(datagramPacket);
        }

    }
}
