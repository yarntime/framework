package com.framework.exception;

public class ApiServerException extends BaseRuntimeException {

    private static final long serialVersionUID = -2561794183487879508L;

    public ApiServerException() {
        super();
    }

    public ApiServerException(String message) {
        super(message);
    }

    public ApiServerException(String message, Throwable th) {
        super(message, th);
    }
}
