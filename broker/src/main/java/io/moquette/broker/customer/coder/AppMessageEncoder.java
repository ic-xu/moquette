package io.moquette.broker.customer.coder;

import io.moquette.broker.customer.constant.CIMConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 服务端发送消息前编码
 */
public class AppMessageEncoder extends MessageToByteEncoder<Transportable> {

	@Override
	protected void encode(final ChannelHandlerContext ctx, final Transportable data, ByteBuf out){
		byte[] body = data.getBody();
		byte[] header = createHeader(data.getType(), body.length);
		out.writeBytes(header);
		out.writeBytes(body);
	}


	/**
	 * 创建消息头，结构为 TLV格式（Tag,Length,Value）
	 * 第一字节为消息类型
	 * 第二，三字节为消息长度分隔为高低位2个字节
	 */
	private byte[] createHeader(byte type, int length) {
		byte[] header = new byte[CIMConstant.DATA_HEADER_LENGTH];
		header[0] = type;
		header[1] = (byte) (length & 0xff);
		header[2] = (byte) ((length >> 8) & 0xff);
		return header;
	}
}
