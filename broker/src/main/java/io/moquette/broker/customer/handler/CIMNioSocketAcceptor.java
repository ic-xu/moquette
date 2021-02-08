package io.moquette.broker.customer.handler;

import io.moquette.broker.customer.coder.AppMessageDecoder;
import io.moquette.broker.customer.coder.AppMessageEncoder;
import io.moquette.broker.customer.coder.WebMessageDecoder;
import io.moquette.broker.customer.coder.WebMessageEncoder;
import io.moquette.broker.customer.constant.CIMConstant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class CIMNioSocketAcceptor{
	private static final Logger LOGGER = LoggerFactory.getLogger(CIMNioSocketAcceptor.class);

	private final HashMap<String, CIMRequestHandler> innerHandlerMap = new HashMap<>();

	private final Integer appPort;
	private final Integer webPort;
	private final CIMRequestHandler outerRequestHandler;
    private final ChannelHandler channelEventHandler;

	/**
	 *  读空闲时间(秒)
	 */
	public static final int READ_IDLE_TIME = 150;

	/**
	 *  写接空闲时间(秒)
	 */
	public static final int WRITE_IDLE_TIME = 120;

	/**
	 * 心跳响应 超时为30秒
	 */
	public static final int PONG_TIME_OUT = 10;

	private CIMNioSocketAcceptor(Builder builder){
		this.webPort = builder.webPort;
		this.appPort = builder.appPort;
		this.channelEventHandler = builder.channelEventHandler;
		this.outerRequestHandler = builder.outerRequestHandler;
	}


	public void destroy(EventLoopGroup bossGroup , EventLoopGroup workerGroup) {
		if(bossGroup != null && !bossGroup.isShuttingDown() && !bossGroup.isShutdown() ) {
			try {bossGroup.shutdownGracefully();}catch(Exception ignore) {}
		}

		if(workerGroup != null && !workerGroup.isShuttingDown() && !workerGroup.isShutdown() ) {
			try {workerGroup.shutdownGracefully();}catch(Exception ignore) {}
		}
	}


	public void bindAppPort(NioEventLoopGroup bossGroup,NioEventLoopGroup workerGroup){
		ServerBootstrap bootstrap = createServerBootstrap(bossGroup,workerGroup);
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch){
				ch.pipeline().addLast(new AppMessageDecoder());
				ch.pipeline().addLast(new AppMessageEncoder());
				ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
				ch.pipeline().addLast(new IdleStateHandler(READ_IDLE_TIME, WRITE_IDLE_TIME, 0));
				ch.pipeline().addLast(channelEventHandler);
			}
		});

		ChannelFuture channelFuture = bootstrap.bind(appPort).syncUninterruptibly();
		channelFuture.channel().newSucceededFuture().addListener(future -> {
			String logBanner = "\n\n" +
					"* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n" +
					"*                                                                                   *\n" +
					"*                                                                                   *\n" +
					"*                   App Socket Server started on port {}.                        *\n" +
					"*                                                                                   *\n" +
					"*                                                                                   *\n" +
					"* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n";
			LOGGER.info(logBanner, appPort);
		});
		channelFuture.channel().closeFuture();
	}

    public void bindWebPort(NioEventLoopGroup bossGroup,NioEventLoopGroup workerGroup){
		ServerBootstrap bootstrap = createServerBootstrap(bossGroup,workerGroup);
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			public void initChannel(SocketChannel ch){
				ch.pipeline().addLast(new HttpServerCodec());
				ch.pipeline().addLast(new ChunkedWriteHandler());
				ch.pipeline().addLast(new HttpObjectAggregator(65536));
				ch.pipeline().addLast(new WebMessageEncoder());
				ch.pipeline().addLast(new WebMessageDecoder());
				ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
				ch.pipeline().addLast(channelEventHandler);
			}

		});

		ChannelFuture channelFuture = bootstrap.bind(webPort).syncUninterruptibly();
		channelFuture.channel().newSucceededFuture().addListener(future -> {
			String logBanner = "\n\n" +
					"* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n" +
					"*                                                                                   *\n" +
					"*                                                                                   *\n" +
					"*                   Websocket Server started on port {}.                         *\n" +
					"*                                                                                   *\n" +
					"*                                                                                   *\n" +
					"* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n";
			LOGGER.info(logBanner, webPort);
		});
		channelFuture.channel().closeFuture();
	}

	private ServerBootstrap createServerBootstrap(EventLoopGroup bossGroup, EventLoopGroup workerGroup){
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.channel(NioServerSocketChannel.class);
		return bootstrap;
	}



	public static class Builder{

		private Integer appPort;
		private Integer webPort;
		private CIMRequestHandler outerRequestHandler;
        private ChannelHandler channelEventHandler;

		public Builder setAppPort(Integer appPort) {
			this.appPort = appPort;
			return this;
		}

        public Builder setChannelEventHandler(ChannelHandler channelEventHandler) {
            this.channelEventHandler = channelEventHandler;
            return this;
        }

        public Builder setWebsocketPort(Integer port) {
			this.webPort = port;
			return this;
		}

		/**
		 * 设置应用层的sentBody处理handler
		 */
		public Builder setOuterRequestHandler(CIMRequestHandler outerRequestHandler) {
			this.outerRequestHandler = outerRequestHandler;
			return this;
		}

		public CIMNioSocketAcceptor build(){
			return new CIMNioSocketAcceptor(this);
		}

	}

}
