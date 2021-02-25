package io.moquette.broker.handler;

import com.alibaba.fastjson.JSON;
import io.moquette.broker.SessionRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;


public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {

    private final SessionRegistry sessions;

    public NettyHttpServerHandler(SessionRegistry sessions) {
        this.sessions = sessions;
    }


    private String result = "";

    /*
     * 收到消息时，返回信息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof FullHttpRequest)) {
            result = "未知请求!";
            send(ctx, result, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        FullHttpRequest httpRequest = (FullHttpRequest) msg;
        try {
            String path = httpRequest.uri();          //获取路径
            String body = getBody(httpRequest);     //获取参数
            HttpMethod method = httpRequest.method();//获取请求方法
            DefaultHttpHeaders headers = (DefaultHttpHeaders)httpRequest.headers();
            String authorization = headers.get("Authorization");

            switch (path) {
                case "/getuser":
                    break;
                case "":
                    break;
                default:
                    send(ctx, sessions.getClientIdByUsername("admin"), HttpResponseStatus.OK);
                    break;

            }
        } catch (Exception e) {
            System.out.println("处理请求失败!");
            e.printStackTrace();
        } finally {
            //释放请求
            httpRequest.release();
        }
    }

    /**
     * 获取body参数
     *
     * @param request
     * @return
     */
    private String getBody(FullHttpRequest request) {
        ByteBuf buf = request.content();
        return buf.toString(CharsetUtil.UTF_8);
    }

    /**
     * 发送的返回值
     *
     * @param ctx     返回
     * @param context 消息
     * @param status  状态
     */
    private void send(ChannelHandlerContext ctx, Object context, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(JSON.toJSONString(context), CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /*
     * 建立连接时，返回消息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("连接的客户端地址:" + ctx.channel().remoteAddress());
//        ctx.writeAndFlush("客户端" + InetAddress.getLocalHost().getHostName() + "成功与服务端建立连接！ ");
        super.channelActive(ctx);
    }

}
