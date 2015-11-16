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
