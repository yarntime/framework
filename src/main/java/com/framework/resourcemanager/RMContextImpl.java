package com.framework.resourcemanager;

import com.framework.message.Dispatcher;

public class RMContextImpl implements RMContext {

    private Dispatcher dispatcher;

    @Override
    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    public String getIdentification() {
        return null;
    }

}
