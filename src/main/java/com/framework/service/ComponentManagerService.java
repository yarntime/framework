package com.framework.service;

import java.util.concurrent.ConcurrentMap;

import com.framework.resourcemanager.RMContext;
import com.framework.response.ResponseObject;

public abstract class ComponentManagerService extends ComponentService {

    public ComponentManagerService(String name, RMContext _rmContext) {
        super(name, _rmContext);
    }

    public abstract ConcurrentMap<String, ? extends ResponseObject> getManagedInstances();
}
