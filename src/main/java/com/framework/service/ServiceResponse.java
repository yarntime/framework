package com.framework.service;

import com.framework.response.ResponseObject;

public class ServiceResponse implements ResponseObject {

    private String name;
    
    private Service.STATE state;
    
    private int level;
    
    public ServiceResponse(int _level, String _name, Service.STATE _state) {
        this.level = (_level + 1) / 2;
        StringBuilder prefix = new StringBuilder(); 
        while (_level-- > 0) {
            prefix.append("-");
        }
        this.name = prefix + "|" + _name;
        this.state = _state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Service.STATE getState() {
        return state;
    }

    public void setState(Service.STATE state) {
        this.state = state;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
