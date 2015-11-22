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

import org.apache.log4j.Logger;

public class TaskBase implements Task {

    private static Logger logger = Logger.getLogger(TaskBase.class);
    
    private TaskProcessor processor;
    
    private TaskCallBack taskCallback;
    
    private String msgUUID;
    
    private TaskState taskState = TaskState.Waiting;

    public TaskProcessor getTaskProcessor() {
        return processor;
    }

    public void setProcessor(TaskProcessor processor) {
        this.processor = processor;
    }

    public TaskCallBack getTaskCallBack() {
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
    public TaskState getTaskState() {
        return taskState;
    }

    @Override
    public Object handle(TaskMsg message) {
        this.msgUUID = message.getMsgUUID();
        this.processor = message.getProcessor();
        this.taskCallback = message.getTaskCallback();
        this.taskState = TaskState.Processing;

        try {
            this.processor.process();
            this.taskCallback.success();
        } catch (Exception e) {
            logger.error("failed to process task " + this.getMsgUUID() + " cause by "
                    + e.getMessage());
            this.taskCallback.error(e.getMessage());
        }
        this.taskState = TaskState.Finish;

        return this;
    }
}
