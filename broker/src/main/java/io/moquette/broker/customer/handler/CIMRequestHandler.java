package io.moquette.broker.customer.handler;

import io.moquette.broker.Session;
import io.moquette.broker.customer.coder.Transportable;

/**
 *  请求处理接口,所有的请求实现必须实现此接口
 */


public interface CIMRequestHandler {

	/**
	 * 处理收到客户端从长链接发送的数据
	 */
	void process(Session session, Transportable message);
}
