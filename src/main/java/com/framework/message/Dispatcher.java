package com.framework.message;

@SuppressWarnings("rawtypes")
public interface Dispatcher {

    public static final String DISPATCHER_EXIT_ON_ERROR_KEY = "driver.dispatcher.exit-on-error";

    public static final boolean DEFAULT_DISPATCHER_EXIT_ON_ERROR = false;

    MessageHandler getMessageHandler();

    void register(Class<?> messageType, MessageHandler handler);

}
