package io.handler.codec.mqtt;

/**
 * Return Code of {@link MqttConnAckMessage}
 * 值 返回码响应 描述
 * 0 0x00连接已接受 连接已被服务端接受
 * 1 0x01连接已拒绝，不支持的协议版本 服务端不支持客户端请求的MQTT协议级别
 * 2 0x02连接已拒绝，不合格的客户端标识符 客户端标识符是正确的UTF-8编码，但服务端不允许使用
 * 3 0x03连接已拒绝，服务端不可用 网络连接已建立，但MQTT服务不可用
 * 4 0x04连接已拒绝，无效的用户名或密码	用户名或密码的数据格式无效
 * 5 0x05连接已拒绝，未授权 客户端未被授权连接到此服务器
 * 6-255 保留
 */
public enum MqttConnectReturnCode {
    CONNECTION_ACCEPTED((byte) 0x00),
    //MQTT 3 codes
    CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION((byte) 0X01),
    CONNECTION_REFUSED_IDENTIFIER_REJECTED((byte) 0x02),
    CONNECTION_REFUSED_SERVER_UNAVAILABLE((byte) 0x03),
    CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD((byte) 0x04),
    CONNECTION_REFUSED_NOT_AUTHORIZED((byte) 0x05),
    //MQTT 5 codes
    CONNECTION_REFUSED_UNSPECIFIED_ERROR((byte) 0x80),
    CONNECTION_REFUSED_MALFORMED_PACKET((byte) 0x81),
    CONNECTION_REFUSED_PROTOCOL_ERROR((byte) 0x82),
    CONNECTION_REFUSED_IMPLEMENTATION_SPECIFIC((byte) 0x83),
    CONNECTION_REFUSED_UNSUPPORTED_PROTOCOL_VERSION((byte) 0x84),
    CONNECTION_REFUSED_CLIENT_IDENTIFIER_NOT_VALID((byte) 0x85),
    CONNECTION_REFUSED_BAD_USERNAME_OR_PASSWORD((byte) 0x86),
    CONNECTION_REFUSED_NOT_AUTHORIZED_5((byte) 0x87),
    CONNECTION_REFUSED_SERVER_UNAVAILABLE_5((byte) 0x88),
    CONNECTION_REFUSED_SERVER_BUSY((byte) 0x89),
    CONNECTION_REFUSED_BANNED((byte) 0x8A),
    CONNECTION_REFUSED_BAD_AUTHENTICATION_METHOD((byte) 0x8C),
    CONNECTION_REFUSED_TOPIC_NAME_INVALID((byte) 0x90),
    CONNECTION_REFUSED_PACKET_TOO_LARGE((byte) 0x95),
    CONNECTION_REFUSED_QUOTA_EXCEEDED((byte) 0x97),
    CONNECTION_REFUSED_PAYLOAD_FORMAT_INVALID((byte) 0x99),
    CONNECTION_REFUSED_RETAIN_NOT_SUPPORTED((byte) 0x9A),
    CONNECTION_REFUSED_QOS_NOT_SUPPORTED((byte) 0x9B),
    CONNECTION_REFUSED_USE_ANOTHER_SERVER((byte) 0x9C),
    CONNECTION_REFUSED_SERVER_MOVED((byte) 0x9D),
    CONNECTION_REFUSED_CONNECTION_RATE_EXCEEDED((byte) 0x9F);

    private static final MqttConnectReturnCode[] VALUES;

    static {
        MqttConnectReturnCode[] values = values();
        VALUES = new MqttConnectReturnCode[160];
        for (MqttConnectReturnCode code : values) {
            final int unsignedByte = code.byteValue & 0xFF;
            // Suppress a warning about out of bounds access since the enum contains only correct values
            VALUES[unsignedByte] = code;    // lgtm [java/index-out-of-bounds]
        }
    }

    private final byte byteValue;

    MqttConnectReturnCode(byte byteValue) {
        this.byteValue = byteValue;
    }

    public byte byteValue() {
        return byteValue;
    }

    public static MqttConnectReturnCode valueOf(byte b) {
        final int unsignedByte = b & 0xFF;
        MqttConnectReturnCode mqttConnectReturnCode = null;
        try {
            mqttConnectReturnCode = VALUES[unsignedByte];
        } catch (ArrayIndexOutOfBoundsException ignored) {
            // no op
        }
        if (mqttConnectReturnCode == null) {
            throw new IllegalArgumentException("unknown connect return code: " + unsignedByte);
        }
        return mqttConnectReturnCode;
    }
}
