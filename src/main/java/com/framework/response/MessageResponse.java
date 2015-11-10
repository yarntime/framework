package com.framework.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class MessageResponse implements ResponseObject {

    @SerializedName("id")
    private String uuid;

    @SerializedName("tasks")
    private List<BaseResponse> tasks;

}
