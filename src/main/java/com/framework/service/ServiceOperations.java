package com.framework.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class ServiceOperations {
    private static final Log LOG = LogFactory.getLog(AbstractService.class);

    private ServiceOperations() {}

    public static void stop(Service service) {
        if (service != null) {
            service.stop();
        }
    }

    public static Exception stopQuietly(Service service) {
        return stopQuietly(LOG, service);
    }

    public static Exception stopQuietly(Log log, Service service) {
        try {
            stop(service);
        } catch (Exception e) {
            log.warn("When stopping the service " + service.getName() + " : " + e, e);
            return e;
        }
        return null;
    }

    public static class ServiceListeners {

        private final List<ServiceStateChangeListener> listeners =
                new ArrayList<ServiceStateChangeListener>();

        public synchronized void add(ServiceStateChangeListener l) {
            if (!listeners.contains(l)) {
                listeners.add(l);
            }
        }

        public synchronized boolean remove(ServiceStateChangeListener l) {
            return listeners.remove(l);
        }

        public synchronized void reset() {
            listeners.clear();
        }

        public void notifyListeners(Service service) {
            // take a very fast snapshot of the callback list
            // very much like CopyOnWriteArrayList, only more minimal
            ServiceStateChangeListener[] callbacks;
            synchronized (this) {
                callbacks = listeners.toArray(new ServiceStateChangeListener[listeners.size()]);
            }
            // iterate through the listeners outside the synchronized method,
            // ensuring that listener registration/unregistration doesn't break
            // anything
            for (ServiceStateChangeListener l : callbacks) {
                l.stateChanged(service);
            }
        }
    }

}
