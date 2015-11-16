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
package com.framework.task;

import com.framework.message.AbstractMessage;
import com.framework.message.MessageType;

@MessageType(messageType = "taskmsg")
public class TaskMsg extends AbstractMessage {

    private TaskProcessor processor;
    private TaskCallBack taskCallback;
    private String msgUUID;

    public TaskMsg(String _msgUUID, TaskProcessor _processor, TaskCallBack _taskCallback) {
        this.msgUUID = _msgUUID;
        this.processor = _processor;
        this.taskCallback = _taskCallback;
    }

    public TaskProcessor getProcessor() {
        return processor;
    }

    public void setProcessor(TaskProcessor processor) {
        this.processor = processor;
    }

    public TaskCallBack getTaskCallback() {
        return taskCallback;
    }

    public void setTaskCallback(TaskCallBack taskCallback) {
        this.taskCallback = taskCallback;
    }

    public String getMsgUUID() {
        return msgUUID;
    }

    public void setMsgUUID(String msgUUID) {
        this.msgUUID = msgUUID;
    }

    @Override
    public Class<?> getMessageType() {
        return TaskMsg.class;
    }
}
