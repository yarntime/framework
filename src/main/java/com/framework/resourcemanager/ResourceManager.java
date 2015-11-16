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
package com.framework.resourcemanager;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.framework.controller.ApiManager;
import com.framework.message.DispatcherType;
import com.framework.message.MessageHandler;
import com.framework.service.ComponentService;
import com.framework.service.ServiceType;
import com.framework.utils.Configuration;

public class ResourceManager extends ComponentService implements MessageHandler<ServiceMsg> {

    private static Logger logger = Logger.getLogger(ResourceManager.class);

    private RMContext rmContext;

    private static String identification = null;

    public ResourceManager() {
        super(ServiceType.ResourceManager.toString(), null);
    }

    public ResourceManager(String _identification, String _url, String _userName, String _password) {
        super(ServiceType.ResourceManager.toString(), null);
        identification = _identification;
    }

    @Override
    protected void serviceInit(Configuration conf) throws Exception {

        logger.info("initing service " + this.getName() + " ...");

        this.rmContext = new RMContextImpl();

        super.serviceInit(conf);

        registeDispatcher(this.dispatcher);

        rmContext.setDispatcher(this.dispatcher);
    }

    @Override
    protected void serviceStart() throws Exception {
        logger.info("starting service " + this.getName() + " ...");
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        logger.info("stopping service " + this.getName() + " ...");
        super.serviceStop();
    }

    public static void main(String argv[]) throws IOException {

        try {
            Configuration conf = new Configuration();

            ResourceManager resourceManager = new ResourceManager();

            resourceManager.init(conf);
            resourceManager.start();

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public Class<?> getMessageType() {
        return ServiceMsg.class;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return DispatcherType.Sync;
    }

    @Override
    public Object handle(ServiceMsg message) {
        return null;
    }
}
