package io.moquette.broker.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.util.concurrent.atomic.AtomicInteger;

public class NettyUdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {

        datagramPacket.retain();
        byte[] buf = new byte[datagramPacket.content().readableBytes()];
        datagramPacket.content().readBytes(buf);
        String s = new String(buf);
        System.out.println(s);
        System.err.println(datagramPacket.sender().getPort());
        ByteBuf byteBuf1 = Unpooled.wrappedBuffer(s.getBytes());
        channelHandlerContext.channel().writeAndFlush(new DatagramPacket(byteBuf1, datagramPacket.sender()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println(cause.getMessage());
    }


}
