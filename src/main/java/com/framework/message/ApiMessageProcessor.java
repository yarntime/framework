package com.framework.message;

import com.framework.resourcemanager.RMContext;

public interface ApiMessageProcessor {

    public Object process(RMContext rmContext, Object instance, ApiMessage message);
}
