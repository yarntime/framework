package com.framework.exception;


public class UnknownMessageException extends BaseRuntimeException {

    private static final long serialVersionUID = 1L;

    public UnknownMessageException() {
        super();
    }

    public UnknownMessageException(String message) {
        super(message);
    }

    public UnknownMessageException(String message, Throwable th) {
        super(message, th);
    }

}
