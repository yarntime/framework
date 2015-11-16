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

import org.apache.log4j.Logger;

import com.framework.exception.BaseRuntimeException;
import com.framework.exception.ServiceNotStartedException;
import com.framework.service.AbstractService;
import com.framework.service.Service.STATE;

public class SyncDispatcher extends AbstractService implements Dispatcher {

    private static Logger logger = Logger.getLogger(SyncDispatcher.class);

    private MessageHandler handlerInstance = null;

    protected final Map<Class<?>, MessageHandler> messageDispatchers;

    public SyncDispatcher(String service) {
        super(service + "-SyncDispatcher");
        this.messageDispatchers = new HashMap<Class<?>, MessageHandler>();
    }

    @Override
    public MessageHandler getMessageHandler() {
        if (handlerInstance == null) {
            handlerInstance = new GenericMessageHandler();
        }
        return handlerInstance;
    }

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

    @SuppressWarnings("unchecked")
    protected Object dispatch(Message message) {
        // all events go thru this loop
        if (logger.isDebugEnabled()) {
            logger.trace(this.getName() + " Dispatching the message "
                    + message.getClass().getSimpleName());
        }

        try {
            MessageHandler handler = messageDispatchers.get(message.getMessageType());

            if (handler != null) {
                return handler.handle(message);
            } else {
                throw new Exception("No handler for registered for " + message.getMessageType());
            }
        } catch (Throwable t) {
            logger.fatal("Error in dispatcher thread " + t.getMessage());
            throw new BaseRuntimeException(t.getMessage());
        }
    }

    class GenericMessageHandler implements MessageHandler<Message> {
        public Object handle(Message message) {
            if (getServiceState() != STATE.STARTED) {
                throw new ServiceNotStartedException("service " + getName() + " has not started...");
            }
            return dispatch(message);
        };
    }

}
