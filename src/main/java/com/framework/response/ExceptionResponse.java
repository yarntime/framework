package com.framework.response;


public class ExceptionResponse implements ResponseObject {

    private Integer errorCode;

    private String errorText;

    public ExceptionResponse(Integer errorCode, String errorText) {
        this.errorCode = errorCode;
        this.errorText = errorText;
    }

    public static BaseResponse buildExceptionResponse(Integer errorCode, String errorText) {
        ExceptionResponse response = new ExceptionResponse(errorCode, errorText);
        BaseResponse result = new BaseResponse(false, response);
        return result;
    }

    public static BaseResponse buildExceptionResponse(Integer errorCode, String errorText,
            String messageId) {
        ExceptionResponse response = new ExceptionResponse(errorCode, errorText);
        BaseResponse result = new BaseResponse(false, response, messageId);
        return result;
    }

}
