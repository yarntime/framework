package com.framework.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.framework.utils.Configuration;

public interface Service extends Closeable {

    public enum STATE {

        NOTINITED(0, "NOTINITED"),

        INITED(1, "INITED"),

        STARTED(2, "STARTED"),

        STOPPED(3, "STOPPED");

        private final int value;

        private final String statename;

        private STATE(int value, String name) {
            this.value = value;
            this.statename = name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return statename;
        }
    }

    void init(Configuration config);

    void start();

    void stop();

    void close() throws IOException;

    void registerServiceListener(ServiceStateChangeListener listener);

    void unregisterServiceListener(ServiceStateChangeListener listener);

    String getName();

    Configuration getConfig();

    STATE getServiceState();

    long getStartTime();

    boolean isInState(STATE state);

    Throwable getFailureCause();

    STATE getFailureState();

    boolean waitForServiceToStop(long timeout);

    public List<LifecycleEvent> getLifecycleHistory();

    public Map<String, String> getBlockers();
}
