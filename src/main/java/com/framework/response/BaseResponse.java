package com.framework.response;


public class BaseResponse {

    private boolean success;

    private ResponseObject response;

    private String cause;

    private String messageId;

    private String identification;

    public static BaseResponse buildResponse(ResponseObject response) {
        return new BaseResponse(true, response);
    }

    public static BaseResponse buildResponse(ResponseObject response, String messageId) {
        return new BaseResponse(true, response, messageId);
    }

    public BaseResponse(boolean success, ResponseObject response) {
        this.success = success;
        this.response = response;
    }

    public BaseResponse(boolean success, ResponseObject response, String messageId) {
        this.success = success;
        this.response = response;
        this.messageId = messageId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ResponseObject getResponse() {
        return response;
    }

    public void setResponse(ResponseObject response) {
        this.response = response;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getIdentification() {
        return this.identification;
    }
    
    public void setIdentification(String identification) {
        this.identification = identification;
    }
}
