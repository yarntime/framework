package com.framework.message;

public interface MessageHandler<T extends Message> {

    Object handle(T message);

}
