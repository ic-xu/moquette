package io.moquette.broker.expression;

public class SessionCorruptedException extends RuntimeException {

    private static final long serialVersionUID = 5848069213104389412L;

    public SessionCorruptedException(String msg) {
        super(msg);
    }
}
