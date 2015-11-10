package com.framework.exception;

public class BaseRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 6872536549171339729L;

    public BaseRuntimeException() {
        super();
    }

    public BaseRuntimeException(String message) {
        super(message);
    }

    public BaseRuntimeException(String message, Throwable th) {
        super(message, th);
    }
}
