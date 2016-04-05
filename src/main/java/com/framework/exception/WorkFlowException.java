/**
 * 
 * WorkFlow exception
 *
 * @author: zhangwei
 * @date: Mar 24, 2016
 * @version: 1.0
 */
package com.framework.exception;

public class WorkFlowException extends Exception {


    /**
     * 
     */
    private static final long serialVersionUID = 4409868583958096006L;

    public WorkFlowException() {}

    public WorkFlowException(String message) {
        super(message);
    }

    public WorkFlowException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkFlowException(Throwable cause) {
        super(cause);
    }

}
