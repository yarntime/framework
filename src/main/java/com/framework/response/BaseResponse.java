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
package com.framework.response;


public class BaseResponse {

    private boolean success;

    private ResponseObject response;

    private String cause;

    private String messageId;

    private String identification;

    public static BaseResponse buildResponse(ResponseObject response) {
        return new BaseResponse(true, response);
    }

    public static BaseResponse buildResponse(ResponseObject response, String messageId) {
        return new BaseResponse(true, response, messageId);
    }

    public BaseResponse(boolean success, ResponseObject response) {
        this.success = success;
        this.response = response;
    }

    public BaseResponse(boolean success, ResponseObject response, String messageId) {
        this.success = success;
        this.response = response;
        this.messageId = messageId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ResponseObject getResponse() {
        return response;
    }

    public void setResponse(ResponseObject response) {
        this.response = response;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getIdentification() {
        return this.identification;
    }
    
    public void setIdentification(String identification) {
        this.identification = identification;
    }
}
