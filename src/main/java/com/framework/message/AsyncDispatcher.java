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
package com.framework.message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.framework.exception.BaseRuntimeException;
import com.framework.exception.ServiceNotStartedException;
import com.framework.response.BaseResponse;
import com.framework.service.AbstractService;
import com.framework.utils.Configuration;

public class AsyncDispatcher extends AbstractService implements Dispatcher {

    private static Logger logger = Logger.getLogger(AsyncDispatcher.class);

    private final BlockingQueue<Message> messageQueue;
    private volatile boolean stopped = false;

    private volatile boolean drainMessagesOnStop = false;

    private volatile boolean drained = true;
    private Object waitForDrained = new Object();

    private volatile boolean blockNewEvents = false;
    private MessageHandler handlerInstance = null;

    private Thread messageHandlingThread;
    protected final Map<Class<?>, MessageHandler> messageDispatchers;
    private boolean exitOnDispatchException;

    public AsyncDispatcher(String service) {
        this(new LinkedBlockingQueue<Message>(), service);
    }

    public AsyncDispatcher(BlockingQueue<Message> messageQueue, String service) {
        super(service + "-AsyncDispatcher");
        this.messageQueue = messageQueue;
        this.messageDispatchers = new HashMap<Class<?>, MessageHandler>();
    }

    Runnable createThread() {
        return new Runnable() {
            @Override
            public void run() {
                while (!stopped && !Thread.currentThread().isInterrupted()) {
                    drained = messageQueue.isEmpty();
                    if (blockNewEvents) {
                        synchronized (waitForDrained) {
                            if (drained) {
                                waitForDrained.notify();
                            }
                        }
                    }
                    Message message = null;
                    try {
                        message = messageQueue.take();
                        if (message != null) {
                            dispatch(message);
                        }
                    } catch (InterruptedException ie) {
                        if (!stopped) {
                            logger.fatal("AsyncDispatcher thread interrupted", ie);
                        }
                        return;
                    } catch (Exception e) {
                        logger.error("failed to handle message " + message.getUUID() + " cause by "
                                + e.getMessage());
                    }
                }
            }
        };
    }

    @Override
    protected void serviceInit(Configuration conf) throws Exception {
        this.exitOnDispatchException = false;
        super.serviceInit(conf);
    }

    @Override
    protected void serviceStart() throws Exception {
        // start all the components
        super.serviceStart();
        messageHandlingThread = new Thread(createThread());
        messageHandlingThread.setName("AsyncDispatcher message handler");
        messageHandlingThread.start();
    }

    public void setDrainEventsOnStop() {
        drainMessagesOnStop = true;
    }

    @Override
    protected void serviceStop() throws Exception {
        if (drainMessagesOnStop) {
            blockNewEvents = true;
            logger.info("AsyncDispatcher is draining to stop, igonring any new events.");
            synchronized (waitForDrained) {
                while (!drained && messageHandlingThread.isAlive()) {
                    waitForDrained.wait(1000);
                    logger.info("Waiting for AsyncDispatcher to drain.");
                }
            }
        }
        stopped = true;
        if (messageHandlingThread != null) {
            messageHandlingThread.interrupt();
            try {
                messageHandlingThread.join();
            } catch (InterruptedException ie) {
                logger.warn("Interrupted Exception while stopping", ie);
            }
        }

        // stop all the components
        super.serviceStop();
    }

    @SuppressWarnings("unchecked")
    protected void dispatch(Message message) {
        // all events go thru this loop
        if (logger.isDebugEnabled()) {
            logger.trace(this.getName() + " Dispatching the message "
                    + message.getClass().getSimpleName());
        }

        try {
            MessageHandler handler = messageDispatchers.get(message.getMessageType());

            if (handler != null) {
                handler.handle(message);
            } else {
                throw new Exception("No handler for registered for " + message.getMessageType());
            }
        } catch (Throwable t) {
            logger.fatal("Error in dispatcher thread " + t.getMessage(), t);
            throw new BaseRuntimeException(t.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void register(Class<?> messageType, MessageHandler handler) {
        MessageHandler<? extends Message> registeredHandler =
                (MessageHandler<? extends Message>) messageDispatchers.get(messageType);
        logger.info(this.getName() + ":" + " Registering " + messageType + " for "
                + handler.getClass().getSimpleName());
        if (registeredHandler == null) {
            messageDispatchers.put(messageType, handler);
        }
    }

    @Override
    public MessageHandler getMessageHandler() {
        if (handlerInstance == null) {
            handlerInstance = new GenericMessageHandler();
        }
        return handlerInstance;
    }

    class GenericMessageHandler implements MessageHandler<Message> {
        public Object handle(Message message) {

            if (getServiceState() != STATE.STARTED) {
                throw new ServiceNotStartedException("service " + getName() + " has not started...");
            }

            if (blockNewEvents) {
                return null;
            }
            drained = false;

            /* all this method does is enqueue all the events onto the queue */
            int qSize = messageQueue.size();
            logger.trace(getName() + "'s queue current size is " + qSize);
            if (qSize != 0 && qSize % 1000 == 0) {
                logger.info("Size of message-queue is " + qSize);
            }
            int remCapacity = messageQueue.remainingCapacity();
            if (remCapacity < 1000) {
                logger.warn("Very low remaining capacity in the message-queue: " + remCapacity);
            }
            try {
                messageQueue.put(message);
            } catch (InterruptedException e) {
                if (!stopped) {
                    logger.warn("AsyncDispatcher thread interrupted", e);
                }
                throw new RuntimeException(e);
            }
            return BaseResponse.buildResponse(message, message.getUUID());
        };
    }
}
