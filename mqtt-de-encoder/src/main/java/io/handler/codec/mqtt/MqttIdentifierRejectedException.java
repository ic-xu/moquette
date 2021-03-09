package io.handler.codec.mqtt;

import io.netty.handler.codec.DecoderException;

/**
 * A {@link MqttIdentifierRejectedException} which is thrown when a CONNECT request contains invalid client identifier.
 */
public final class MqttIdentifierRejectedException extends DecoderException {

    private static final long serialVersionUID = -1323503322689614981L;

    /**
     * Creates a new instance
     */
    public MqttIdentifierRejectedException() { }

    /**
     * Creates a new instance
     */
    public MqttIdentifierRejectedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance
     */
    public MqttIdentifierRejectedException(String message) {
        super(message);
    }

    /**
     * Creates a new instance
     */
    public MqttIdentifierRejectedException(Throwable cause) {
        super(cause);
    }

}
