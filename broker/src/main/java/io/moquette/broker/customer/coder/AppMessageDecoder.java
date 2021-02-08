package io.moquette.broker.customer.coder;

import io.moquette.broker.customer.constant.CIMConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 服务端接收消息路由解码，通过消息类型分发到不同的真正解码器
 */
public class AppMessageDecoder extends ByteToMessageDecoder {

 

	@Override
	protected void decode(ChannelHandlerContext arg0, ByteBuf buffer, List<Object> queue) throws Exception {
		
		/*
		 * 消息体不足3位，发生断包情况
		 */
		if (buffer.readableBytes() < CIMConstant.DATA_HEADER_LENGTH) {
			return;
		}

		buffer.markReaderIndex();

		byte type = buffer.readByte();

		byte lv = buffer.readByte();
		byte hv = buffer.readByte();
		int length = getContentLength(lv, hv);

		/*
		 * 发生断包情况，等待接收完成
		 */
		if (buffer.readableBytes() < length) {
			buffer.resetReaderIndex();
			return;
		}
		 
		byte[] dataBytes = new byte[length];
		buffer.readBytes(dataBytes);


		Object message = mappingMessageObject(dataBytes, type);
		
		queue.add(message);
	}

	public Object mappingMessageObject(byte[] data, byte type) throws Exception {

//		if (CIMConstant.ProtobufType.C_H_RS == type) {
//			return HeartbeatResponse.getInstance();
//		}
//
//		SentBodyProto.Model bodyProto = SentBodyProto.Model.parseFrom(data);
//		SentBody body = new SentBody();
//		body.setKey(bodyProto.getKey());
//		body.setTimestamp(bodyProto.getTimestamp());
//		body.putAll(bodyProto.getDataMap());

		return new String(data, Charset.forName("UTF-8"));
	}

	/**
	 * 解析消息体长度
	 * 最大消息长度为2个字节表示的长度，即为65535
	 * @param lv 低位1字节消息长度
	 * @param hv 高位1字节消息长度
	 * @return 消息的真实长度
	 */
	private int getContentLength(byte lv, byte hv) {
		int l = (lv & 0xff);
		int h = (hv & 0xff);
		return l | h << 8;
	}

}
