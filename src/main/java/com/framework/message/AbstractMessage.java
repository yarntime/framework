package com.framework.message;

import java.util.Map;
import java.util.UUID;

public abstract class AbstractMessage implements Message {

    protected Map<String, Object> header;

    protected long time;

    protected String uuid;

    public AbstractMessage() {
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public Map<String, Object> getHeader() {
        return header;
    }

    @Override
    public long getTimestamp() {
        return time;
    }

    @Override
    public String getUUID() {
        return this.uuid;
    }

}
