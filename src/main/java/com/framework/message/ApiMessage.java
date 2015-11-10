package com.framework.message;

public interface ApiMessage {

    public Class<?> getMessageProcessor();

    public String getEventType();
}
