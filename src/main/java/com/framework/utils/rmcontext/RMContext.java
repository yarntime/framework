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
package com.framework.utils.rmcontext;

import com.framework.message.Dispatcher;

public class RMContext {

    private static Dispatcher dispatcher;
    
    private static String indentification;

    public static void setDispatcher(Dispatcher _dispatcher) {
        dispatcher = _dispatcher;
    }

    public static Dispatcher getDispatcher() {
        return dispatcher;
    }
    
    public static void setIdentification(String _indentification) {
        indentification = _indentification;
    }

    public static String getIdentification() {
        return indentification;
    }

}
