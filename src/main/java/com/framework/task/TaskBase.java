package com.framework.task;

import org.apache.log4j.Logger;

import com.framework.resourcemanager.RMContext;

public class TaskBase implements Task {

    private static Logger logger = Logger.getLogger(TaskBase.class);

    private RMContext rmContext;
    private TaskProcessor processor;
    private TaskCallBack taskCallback;
    private String msgUUID;
    private TaskState taskState = TaskState.Waiting;

    public TaskBase(RMContext _rmContext) {
        this.rmContext = _rmContext;
    }

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

    @SuppressWarnings("unchecked")
    @Override
    public Object handle(TaskMsg message) {
        this.msgUUID = message.getMsgUUID();
        this.processor = message.getProcessor();
        this.taskCallback = message.getTaskCallback();
        this.taskState = TaskState.Processing;
        EventMsg eventMsg = message.getEventMsg();

        try {
            this.processor.process();
            this.taskCallback.success();
        } catch (Exception e) {
            logger.error("failed to process task " + this.getMsgUUID() + " cause by "
                    + e.getMessage());
            this.taskCallback.error(e.getMessage());
            eventMsg.getResponse().setSuccess(false);
            eventMsg.getResponse().setCause(e.getMessage());
        }
        this.taskState = TaskState.Finish;

        rmContext.getDispatcher().getMessageHandler().handle(eventMsg);

        if (eventMsg.getResponse().isSuccess()) {
            this.processor.afterDone();
        }

        return this;
    }
}
