/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.framework.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.framework.message.AsyncDispatcher;
import com.framework.message.Dispatcher;
import com.framework.message.DispatcherType;
import com.framework.message.MessageHandler;
import com.framework.message.SyncDispatcher;
import com.framework.resourcemanager.RMContext;
import com.framework.utils.Configuration;

public abstract class ComponentService extends CompositeService {

    private static Logger logger = Logger.getLogger(ComponentService.class);

    protected RMContext rmContext;

    protected Dispatcher dispatcher;

    protected void serviceInit(Configuration conf) throws Exception {
        logger.info("initing dispatcher(" + getDispatcherType() + ") for service " + getName());
        this.dispatcher = createDispatcher(getDispatcherType(), conf);
        addIfService(this.dispatcher);
        super.serviceInit(conf);
    }

    public ComponentService(String name, RMContext _rmContext) {
        super(name);
        this.rmContext = _rmContext;
    }

    public abstract Class<?> getMessageType();

    public abstract DispatcherType getDispatcherType();

    public Dispatcher createDispatcher(DispatcherType type, Configuration conf) {
        Dispatcher dispatcher = null;
        if (type == null) {
            return null;
        }
        switch (type) {
            case Sync:
                dispatcher = new SyncDispatcher(this.getName());
                break;
            case Async:
                dispatcher = new AsyncDispatcher(this.getName());
                break;
            default:
                throw new RuntimeException("unknown DispatcherType " + type);
        }
        return dispatcher;
    }

    public void registeDispatcher(Dispatcher topDispatcher) {

        if (this.getMessageType() != null) {
            topDispatcher.register(this.getMessageType(), (MessageHandler<?>) this);
        }

        List<Service> services = getServices();
        for (Service service : services) {
            if (service instanceof ComponentService) {
                ComponentService componentService = (ComponentService) service;
                if (componentService.getMessageType() != null) {
                    logger.info("registing service " + service.getName() + " to top dispatcher...");
                    componentService.registeDispatcher(topDispatcher);
                }
            }
        }
    }

    public Dispatcher getDispatcher() {
        return this.dispatcher;
    }
}
