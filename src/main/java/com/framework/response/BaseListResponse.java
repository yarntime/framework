package com.framework.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class BaseListResponse implements ResponseObject {

    @SerializedName("results")
    private List<ResponseObject> results;

    @SerializedName("identification")
    private String identification;

    public BaseListResponse(List<ResponseObject> results) {
        this.results = results;
    }

    public static BaseResponse buildListResponse(List<ResponseObject> results) {
        BaseListResponse response = new BaseListResponse(results);
        return new BaseResponse(true, response);
    }

    public List<ResponseObject> getResults() {
        return this.results;
    }

}
