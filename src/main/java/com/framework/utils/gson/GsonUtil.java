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
