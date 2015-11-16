package com.framework.task;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.framework.message.DispatcherType;
import com.framework.message.MessageHandler;
import com.framework.resourcemanager.RMContext;
import com.framework.utils.Configuration;
import com.framework.utils.NamedThreadFactory;

public class TaskManager extends TaskService {

    private static Logger logger = Logger.getLogger(TaskManager.class);

    ConcurrentMap<String, Task> tasks = new ConcurrentSkipListMap<String, Task>();

    private ExecutorService poolExecutor;

    public TaskManager(RMContext _rmContext) {
        super(_rmContext);
    }

    @Override
    protected void serviceInit(Configuration conf) throws Exception {
        logger.info("initing service " + this.getName() + " ...");
        super.serviceInit(conf);

        this.dispatcher.register(TaskMsg.class, new TaskDispatcher());

        poolExecutor = Executors.newFixedThreadPool(200, new NamedThreadFactory("TaskThreadPool"));
    }

    @Override
    protected void serviceStart() throws Exception {
        logger.info("starting service " + this.getName() + " ...");
        super.serviceStart();
    }

    class TaskDispatcher implements MessageHandler<TaskMsg> {

        @Override
        public Object handle(TaskMsg message) {
            Task task = new TaskBase(rmContext);
            tasks.put(message.getMsgUUID(), task);
            poolExecutor.execute(new TaskExecutor(task, message));
            return task;
        }
    }

    protected class TaskExecutor implements Runnable {

        private Task task;
        private TaskMsg taskMsg;

        public TaskExecutor(Task _task, TaskMsg _taskMsg) {
            this.task = _task;
            this.taskMsg = _taskMsg;
        }

        @Override
        public void run() {
            task.handle(taskMsg);
        }
    }

    @Override
    public DispatcherType getDispatcherType() {
        return DispatcherType.Async;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object handle(TaskMsg message) {
        return this.dispatcher.getMessageHandler().handle(message);
    }

}
