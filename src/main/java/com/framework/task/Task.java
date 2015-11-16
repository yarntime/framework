package com.framework.task;

import com.framework.message.MessageHandler;

public interface Task extends MessageHandler<TaskMsg> {

    public String getMsgUUID();

    public TaskProcessor getTaskProcessor();

    public TaskCallBack getTaskCallBack();

    public TaskState getTaskState();
}
