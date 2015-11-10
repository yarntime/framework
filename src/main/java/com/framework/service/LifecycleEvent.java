package com.framework.service;

import java.io.Serializable;

public class LifecycleEvent implements Serializable {

    private static final long serialVersionUID = 1648576996238247836L;

    public long time;

    public Service.STATE state;
}
