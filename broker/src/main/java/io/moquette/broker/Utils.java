
package io.moquette.broker;

import io.netty.buffer.ByteBuf;
import io.handler.codec.mqtt.MqttMessage;
import io.handler.codec.mqtt.MqttMessageIdVariableHeader;
import java.util.Map;

/**
 * Utility static methods, like Map get with default value, or elvis operator.
 */
public final class Utils {

    public static <T, K> T defaultGet(Map<K, T> map, K key, T defaultValue) {
        T value = map.get(key);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    public static int messageId(MqttMessage msg) {
        if(null==msg.variableHeader())
            return 0;
        return ((MqttMessageIdVariableHeader) msg.variableHeader()).messageId();
    }

    public static byte[] readBytesAndRewind(ByteBuf payload) {
        byte[] payloadContent = new byte[payload.readableBytes()];
        int mark = payload.readerIndex();
        payload.readBytes(payloadContent);
        payload.readerIndex(mark);
        return payloadContent;
    }

    private Utils() {
    }
}
