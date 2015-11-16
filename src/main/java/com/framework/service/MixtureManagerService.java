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
import java.util.concurrent.ConcurrentMap;

import com.framework.exception.UnknownMessageException;
import com.framework.message.QueryMessage;
import com.framework.resourcemanager.RMContext;
import com.framework.response.BaseListResponse;
import com.framework.response.BaseResponse;
import com.framework.response.ResponseObject;

public abstract class MixtureManagerService extends ComponentService {

    public MixtureManagerService(String name, RMContext _rmContext) {
        super(name, _rmContext);
    }

    public abstract ConcurrentMap<String, ? extends ResponseObject> getManagedInstances();

    protected Object handleQueryMsg(QueryMessage message) {
        List<Service> services = this.getServices();
        services.add(this);
        for (Service service : services) {
            if (service.getName().equalsIgnoreCase(message.getQueryService())) {
                return handle(service, message);
            }
        }
        throw new UnknownMessageException("failed to handle message " + message);
    }

    private Object handle(Service service, QueryMessage message) {
        MixtureManagerService s = (MixtureManagerService) service;
        if (message.getId() != null) {
            ResponseObject instance = s.getManagedInstances().get(message.getId());
            BaseResponse response = BaseResponse.buildResponse(instance);
            return response;
        } else {
            List<ResponseObject> results = new ArrayList<ResponseObject>();
            for (ResponseObject vm : s.getManagedInstances().values()) {
                results.add(vm);
            }
            return BaseListResponse.buildListResponse(results);
        }
    }
}
