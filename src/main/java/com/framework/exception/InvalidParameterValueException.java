package com.framework.exception;

public class InvalidParameterValueException extends BaseException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public InvalidParameterValueException(String message) {
        super(ErrorCode.INVALID_PARAMETER_VAUE, message);
    }

}
