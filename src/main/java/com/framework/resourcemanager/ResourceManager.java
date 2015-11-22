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
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.framework.message.DispatcherType;
import com.framework.message.MessageHandler;
import com.framework.service.ComponentService;
import com.framework.service.ServiceType;
import com.framework.utils.Configuration;
import com.framework.utils.rmcontext.RMContext;

public class ResourceManager extends ComponentService implements MessageHandler<ServiceMsg> {

    private static Logger logger = Logger.getLogger(ResourceManager.class);

    private static String identification = null;

    public ResourceManager() {
        super(ServiceType.ResourceManager.toString());
    }

    @Override
    protected void serviceInit(Configuration conf) throws Exception {

        logger.info("initing service " + this.getName() + " ...");

        super.serviceInit(conf);

        registeDispatcher(this.dispatcher);

        RMContext.setDispatcher(this.dispatcher);
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

        identification = "identification";
         
        logger.info("resource manager is starting  with  identification " + identification + " ...");
        try {
            ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
            
            Configuration conf = new Configuration();

            ResourceManager resourceManager = (ResourceManager) ctx.getBean("resourceManagerService");

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
