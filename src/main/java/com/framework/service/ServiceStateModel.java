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

public class ServiceStateModel {

    private static final boolean[][] statemap = {
            // uninited inited started stopped
            /* uninited */{false, true, false, true},
            /* inited */{false, true, true, true},
            /* started */{false, false, true, true},
            /* stopped */{false, false, false, true},};

    private volatile Service.STATE state;

    private String name;

    public ServiceStateModel(String name) {
        this(name, Service.STATE.NOTINITED);
    }

    public ServiceStateModel(String name, Service.STATE state) {
        this.state = state;
        this.name = name;
    }

    public Service.STATE getState() {
        return state;
    }

    public boolean isInState(Service.STATE proposed) {
        return state.equals(proposed);
    }

    public void ensureCurrentState(Service.STATE expectedState) {
        if (state != expectedState) {
            throw new ServiceStateException(name + ": for this operation, the "
                    + "current service state must be " + expectedState + " instead of " + state);
        }
    }

    public synchronized Service.STATE enterState(Service.STATE proposed) {
        checkStateTransition(name, state, proposed);
        Service.STATE oldState = state;
        // atomic write of the new state
        state = proposed;
        return oldState;
    }

    public static void checkStateTransition(String name, Service.STATE state, Service.STATE proposed) {
        if (!isValidStateTransition(state, proposed)) {
            throw new ServiceStateException(name + " cannot enter state " + proposed
                    + " from state " + state);
        }
    }

    public static boolean isValidStateTransition(Service.STATE current, Service.STATE proposed) {
        boolean[] row = statemap[current.getValue()];
        return row[proposed.getValue()];
    }

    @Override
    public String toString() {
        return (name.isEmpty() ? "" : ((name) + ": ")) + state.toString();
    }

}
