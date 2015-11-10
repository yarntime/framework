package com.framework.exception;


public class ServiceNotStartedException extends BaseRuntimeException {

    private static final long serialVersionUID = 1;

    public ServiceNotStartedException() {
        super();
    }

    public ServiceNotStartedException(String message) {
        super(message);
    }

    public ServiceNotStartedException(String message, Throwable th) {
        super(message, th);
    }
}
