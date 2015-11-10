package com.framework.service;

import java.util.ArrayList;
import java.util.List;

import com.framework.exception.UnknownMessageException;
import com.framework.message.QueryMessage;
import com.framework.resourcemanager.RMContext;
import com.framework.response.BaseListResponse;
import com.framework.response.BaseResponse;
import com.framework.response.ResponseObject;

public abstract class QueryManagerService extends ComponentService {

    public QueryManagerService(String name, RMContext _rmContext) {
        super(name, _rmContext);
    }

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
        ComponentManagerService s = (ComponentManagerService) service;
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
