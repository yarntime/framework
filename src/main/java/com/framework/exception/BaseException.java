package com.framework.exception;

public class BaseException extends Exception {

    private static final long serialVersionUID = 1L;

    private int errorCode;

    public BaseException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String toString() {
        return "BaseException [errorCode=" + errorCode + "]";
    }
}
