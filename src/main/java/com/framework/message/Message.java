package com.framework.message;

import java.util.Map;

import com.framework.response.ResponseObject;

public interface Message extends ResponseObject {

    Map<String, Object> getHeader();

    long getTimestamp();

    String toString();

    String getUUID();

    Class<?> getMessageType();
}
