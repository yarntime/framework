package com.framework.exception;


public class HttpRequestException extends BaseException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int statusCode;

    public HttpRequestException(int statusCode, String message) {
        super(ErrorCode.HTTP_REQUEST_ERROR, message);
        this.statusCode = statusCode;
    }

    public HttpRequestException(String message, int statusCode) {
        super(ErrorCode.HTTP_REQUEST_ERROR, message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return "HttpRequestException [statusCode=" + statusCode + "]";
    }

}
