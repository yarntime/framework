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
package com.framework.utils.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

public class GsonUtil {
    GsonBuilder _gsonBuilder;

    public GsonUtil() {
        _gsonBuilder = new GsonBuilder();
    }

    public GsonUtil setCoder(Class<?> clazz, GsonTypeCoder<?> coder) {
        _gsonBuilder.registerTypeAdapter(clazz, coder);
        return this;
    }

    public GsonUtil setExclusionStrategies(ExclusionStrategy[] excludeStrateges) {
        _gsonBuilder.setExclusionStrategies(excludeStrateges);
        return this;
    }

    public GsonUtil setInstanceCreator(Class<?> clazz, InstanceCreator<?> creator) {
        _gsonBuilder.registerTypeAdapter(clazz, creator);
        return this;
    }

    public GsonUtil enableNullDecoder() {
        _gsonBuilder.serializeNulls();
        return this;
    }

    public Gson create() {
        _gsonBuilder.setVersion(1.7);
        _gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return _gsonBuilder.create();
    }
}
