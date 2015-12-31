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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.framework.response.ResponseObject;
import com.framework.utils.Configuration;

public abstract class AbstractService implements Service {

    private static final Log LOG = LogFactory.getLog(AbstractService.class);

    private final String name;

    private final ServiceStateModel stateModel;

    private long startTime;

    private volatile Configuration config;

    private final ServiceOperations.ServiceListeners listeners =
            new ServiceOperations.ServiceListeners();

    private static ServiceOperations.ServiceListeners globalListeners =
            new ServiceOperations.ServiceListeners();

    private Exception failureCause;

    private STATE failureState = null;

    private final AtomicBoolean terminationNotification = new AtomicBoolean(false);

    private final List<LifecycleEvent> lifecycleHistory = new ArrayList<LifecycleEvent>(5);

    private final Map<String, String> blockerMap = new HashMap<String, String>();

    private final Object stateChangeLock = new Object();

    public AbstractService(String name) {
        this.name = name;
        stateModel = new ServiceStateModel(name);
    }

    @Override
    public final STATE getServiceState() {
        return stateModel.getState();
    }

    @Override
    public final synchronized Throwable getFailureCause() {
        return failureCause;
    }

    @Override
    public synchronized STATE getFailureState() {
        return failureState;
    }

    protected void setConfig(Configuration conf) {
        this.config = conf;
    }

    @Override
    public void init(Configuration conf) {
        if (conf == null) {
            throw new ServiceStateException("Cannot initialize service " + getName()
                    + ": null configuration");
        }
        if (isInState(STATE.INITED)) {
            return;
        }
        synchronized (stateChangeLock) {
            if (enterState(STATE.INITED) != STATE.INITED) {
                setConfig(conf);
                try {
                    serviceInit(config);
                    if (isInState(STATE.INITED)) {
                        // if the service ended up here during init,
                        // notify the listeners
                        notifyListeners();
                    }
                } catch (Exception e) {
                    noteFailure(e);
                    ServiceOperations.stopQuietly(LOG, this);
                    throw ServiceStateException.convert(e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ServiceStateException if the current service state does not permit this action
     */
    @Override
    public void start() {
        if (isInState(STATE.STARTED)) {
            return;
        }
        // enter the started state
        synchronized (stateChangeLock) {
            if (stateModel.enterState(STATE.STARTED) != STATE.STARTED) {
                try {
                    startTime = System.currentTimeMillis();
                    serviceStart();
                    if (isInState(STATE.STARTED)) {
                        // if the service started (and isn't now in a later
                        // state), notify
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Service " + getName() + " is started");
                        }
                        notifyListeners();
                    }
                } catch (Exception e) {
                    noteFailure(e);
                    ServiceOperations.stopQuietly(LOG, this);
                    throw ServiceStateException.convert(e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        if (isInState(STATE.STOPPED)) {
            return;
        }
        synchronized (stateChangeLock) {
            if (enterState(STATE.STOPPED) != STATE.STOPPED) {
                try {
                    serviceStop();
                } catch (Exception e) {
                    // stop-time exceptions are logged if they are the first
                    // one,
                    noteFailure(e);
                    throw ServiceStateException.convert(e);
                } finally {
                    // report that the service has terminated
                    terminationNotification.set(true);
                    synchronized (terminationNotification) {
                        terminationNotification.notifyAll();
                    }
                    // notify anything listening for events
                    notifyListeners();
                }
            } else {
                // already stopped: note it
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Ignoring re-entrant call to stop()");
                }
            }
        }
    }

    @Override
    public final void close() throws IOException {
        stop();
    }

    protected final void noteFailure(Exception exception) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("noteFailure " + exception, null);
        }
        if (exception == null) {
            // make sure failure logic doesn't itself cause problems
            return;
        }
        // record the failure details, and log it
        synchronized (this) {
            if (failureCause == null) {
                failureCause = exception;
                failureState = getServiceState();
                LOG.info("Service " + getName() + " failed in state " + failureState + "; cause: "
                        + exception, exception);
            }
        }
    }

    @Override
    public final boolean waitForServiceToStop(long timeout) {
        boolean completed = terminationNotification.get();
        while (!completed) {
            try {
                synchronized (terminationNotification) {
                    terminationNotification.wait(timeout);
                }
                // here there has been a timeout, the object has terminated,
                // or there has been a spurious wakeup (which we ignore)
                completed = true;
            } catch (InterruptedException e) {
                // interrupted; have another look at the flag
                completed = terminationNotification.get();
            }
        }
        return terminationNotification.get();
    }

    protected void serviceInit(Configuration conf) throws Exception {
        if (conf != config) {
            LOG.debug("Config has been overridden during init");
            setConfig(conf);
        }
    }

    protected void serviceStart() throws Exception {

    }

    protected void serviceStop() throws Exception {

    }

    @Override
    public void registerServiceListener(ServiceStateChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void unregisterServiceListener(ServiceStateChangeListener l) {
        listeners.remove(l);
    }

    public static void registerGlobalListener(ServiceStateChangeListener l) {
        globalListeners.add(l);
    }

    public static boolean unregisterGlobalListener(ServiceStateChangeListener l) {
        return globalListeners.remove(l);
    }

    static void resetGlobalListeners() {
        globalListeners.reset();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public synchronized Configuration getConfig() {
        return config;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    private void notifyListeners() {
        try {
            listeners.notifyListeners(this);
            globalListeners.notifyListeners(this);
        } catch (Throwable e) {
            LOG.warn("Exception while notifying listeners of " + this + ": " + e, e);
        }
    }

    private void recordLifecycleEvent() {
        LifecycleEvent event = new LifecycleEvent();
        event.time = System.currentTimeMillis();
        event.state = getServiceState();
        lifecycleHistory.add(event);
    }

    @Override
    public synchronized List<LifecycleEvent> getLifecycleHistory() {
        return new ArrayList<LifecycleEvent>(lifecycleHistory);
    }

    private STATE enterState(STATE newState) {
        assert stateModel != null : "null state in " + name + " " + this.getClass();
        STATE oldState = stateModel.enterState(newState);
        if (oldState != newState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Service: " + getName() + " entered state " + getServiceState());
            }
            recordLifecycleEvent();
        }
        return oldState;
    }

    @Override
    public final boolean isInState(Service.STATE expected) {
        return stateModel.isInState(expected);
    }

    @Override
    public String toString() {
        return "Service " + name + " in state " + stateModel;
    }

    protected void putBlocker(String name, String details) {
        synchronized (blockerMap) {
            blockerMap.put(name, details);
        }
    }

    public void removeBlocker(String name) {
        synchronized (blockerMap) {
            blockerMap.remove(name);
        }
    }

    @Override
    public Map<String, String> getBlockers() {
        synchronized (blockerMap) {
            Map<String, String> map = new HashMap<String, String>(blockerMap);
            return map;
        }
    }
}
