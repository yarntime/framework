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

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.framework.response.ResponseObject;
import com.framework.utils.Configuration;

public interface Service extends Closeable, ResponseObject {

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
