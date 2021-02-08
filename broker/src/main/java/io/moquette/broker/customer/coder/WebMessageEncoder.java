package io.moquette.broker.customer.coder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

/**
 * websocket发送消息前编码
 */
public class WebMessageEncoder extends MessageToMessageEncoder<Transportable> {

	private static final UnpooledByteBufAllocator BYTE_BUF_ALLOCATOR = new UnpooledByteBufAllocator(false);

	@Override
	protected void encode(ChannelHandlerContext ctx, Transportable data, List<Object> out){
		byte[] body = data.getBody();
		ByteBuf buffer = BYTE_BUF_ALLOCATOR.buffer(body.length + 1);
		buffer.writeByte(data.getType());
		buffer.writeBytes(body);
		out.add(new BinaryWebSocketFrame(buffer));
	}
}
