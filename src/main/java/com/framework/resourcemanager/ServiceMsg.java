package com.framework.resourcemanager;

import com.framework.message.AbstractMessage;

public abstract class ServiceMsg extends AbstractMessage {

    @Override
    public Class<?> getMessageType() {
        return ServiceMsg.class;
    }
}
