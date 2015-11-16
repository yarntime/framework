package com.framework.task;

import com.framework.message.MessageHandler;
import com.framework.resourcemanager.RMContext;
import com.framework.service.ComponentService;

public abstract class TaskService extends ComponentService implements MessageHandler<TaskMsg> {

    public TaskService(RMContext _rmContext) {
        super("TaskService", _rmContext);
    }

    @Override
    public Class<?> getMessageType() {
        return TaskMsg.class;
    }

}
