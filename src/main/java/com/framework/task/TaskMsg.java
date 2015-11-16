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
