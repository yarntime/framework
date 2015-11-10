package com.framework.resourcemanager;

import com.framework.message.Dispatcher;

public interface RMContext {

    Dispatcher getDispatcher();

    void setDispatcher(Dispatcher dispatcher);

    String getIdentification();
}
