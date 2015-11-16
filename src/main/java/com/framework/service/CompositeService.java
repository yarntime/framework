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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.framework.utils.Configuration;


public class CompositeService extends AbstractService {

    private static final Log LOG = LogFactory.getLog(CompositeService.class);

    protected static final boolean STOP_ONLY_STARTED_SERVICES = false;

    private final List<Service> serviceList = new ArrayList<Service>();

    public CompositeService(String name) {
        super(name);
    }

    public List<Service> getServices() {
        synchronized (serviceList) {
            return new ArrayList<Service>(serviceList);
        }
    }

    protected void addService(Service service) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding service " + service.getName());
        }
        synchronized (serviceList) {
            serviceList.add(service);
        }
    }

    protected boolean addIfService(Object object) {
        if (object instanceof Service) {
            addService((Service) object);
            return true;
        } else {
            return false;
        }
    }

    protected synchronized boolean removeService(Service service) {
        synchronized (serviceList) {
            return serviceList.remove(service);
        }
    }

    protected void serviceInit(Configuration conf) throws Exception {
        List<Service> services = getServices();
        if (LOG.isDebugEnabled()) {
            LOG.debug(getName() + ": initing services, size=" + services.size());
        }
        super.serviceInit(conf);
        for (Service service : services) {
            service.init(conf);
        }
    }

    protected void serviceStart() throws Exception {
        List<Service> services = getServices();
        if (LOG.isDebugEnabled()) {
            LOG.debug(getName() + ": starting services, size=" + services.size());
        }
        for (Service service : services) {
            // start the service. If this fails that service
            // will be stopped and an exception raised
            service.start();
        }
        super.serviceStart();
    }

    protected void serviceStop() throws Exception {
        // stop all services that were started
        int numOfServicesToStop = serviceList.size();
        if (LOG.isDebugEnabled()) {
            LOG.debug(getName() + ": stopping services, size=" + numOfServicesToStop);
        }
        stop(numOfServicesToStop, STOP_ONLY_STARTED_SERVICES);
        super.serviceStop();
    }

    private void stop(int numOfServicesStarted, boolean stopOnlyStartedServices) {
        // stop in reverse order of start
        Exception firstException = null;
        List<Service> services = getServices();
        for (int i = numOfServicesStarted - 1; i >= 0; i--) {
            Service service = services.get(i);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stopping service #" + i + ": " + service);
            }
            STATE state = service.getServiceState();
            // depending on the stop police
            if (state == STATE.STARTED || (!stopOnlyStartedServices && state == STATE.INITED)) {
                Exception ex = ServiceOperations.stopQuietly(LOG, service);
                if (ex != null && firstException == null) {
                    firstException = ex;
                }
            }
        }
        // after stopping all services, rethrow the first exception raised
        if (firstException != null) {
            throw ServiceStateException.convert(firstException);
        }
    }

    public static class CompositeServiceShutdownHook implements Runnable {

        private CompositeService compositeService;

        public CompositeServiceShutdownHook(CompositeService compositeService) {
            this.compositeService = compositeService;
        }

        @Override
        public void run() {
            ServiceOperations.stopQuietly(compositeService);
        }
    }

}
