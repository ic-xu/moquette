package io.moquette.broker.customer.constant;

/**
 * 常量
 */
public interface CIMConstant {
	/**
	 消息头长度为3个字节，第一个字节为消息类型，第二，第三字节 转换int后为消息长度
	 */
	int DATA_HEADER_LENGTH = 5;

	String KEY_ACCOUNT = "account";

	String KEY_QUIETLY_CLOSE = "quietlyClose";

	String HEARTBEAT_KEY = "heartbeat";
	
	String CLIENT_HEARTBEAT = "client_heartbeat";
	 
	String CLIENT_CONNECT_CLOSED = "client_closed";
	
	interface ProtobufType {
		byte S_H_RQ = 1;
		byte C_H_RS = 0;
		byte MESSAGE = 2;
		byte REPLY_BODY = 4;
	}

	interface MessageAction {
		/*
		 内置消息类型
		 被其他设备登录挤下线消息
		 */
		String ACTION_OFFLINE = "999";
	}
}
