package com.framework.service;

/**
 * Exception that is raised on state change operations.
 */
public class ServiceStateException extends RuntimeException {

    private static final long serialVersionUID = 1110000352259232646L;

    public ServiceStateException(String message) {
	super(message);
    }

    public ServiceStateException(String message, Throwable cause) {
	super(message, cause);
    }

    public ServiceStateException(Throwable cause) {
	super(cause);
    }

    /**
     * Convert any exception into a {@link RuntimeException}. If the caught
     * exception is already of that type, it is typecast to a
     * {@link RuntimeException} and returned.
     * 
     * All other exception types are wrapped in a new instance of
     * ServiceStateException
     * 
     * @param fault
     *            exception or throwable
     * @return a ServiceStateException to rethrow
     */
    public static RuntimeException convert(Throwable fault) {
	if (fault instanceof RuntimeException) {
	    return (RuntimeException) fault;
	} else {
	    return new ServiceStateException(fault);
	}
    }

    /**
     * Convert any exception into a {@link RuntimeException}. If the caught
     * exception is already of that type, it is typecast to a
     * {@link RuntimeException} and returned.
     * 
     * All other exception types are wrapped in a new instance of
     * ServiceStateException
     * 
     * @param text
     *            text to use if a new exception is created
     * @param fault
     *            exception or throwable
     * @return a ServiceStateException to rethrow
     */
    public static RuntimeException convert(String text, Throwable fault) {
	if (fault instanceof RuntimeException) {
	    return (RuntimeException) fault;
	} else {
	    return new ServiceStateException(text, fault);
	}
    }
}
