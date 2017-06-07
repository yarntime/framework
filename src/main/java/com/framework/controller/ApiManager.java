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
package com.framework.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.framework.header.Api;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.SocketHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.log4j.Logger;

import com.framework.exception.BaseException;
import com.framework.exception.BaseRuntimeException;
import com.framework.exception.HttpRequestException;
import com.framework.message.DispatcherType;
import com.framework.message.Message;
import com.framework.message.MessageBuilder;
import com.framework.response.BaseResponse;
import com.framework.response.ExceptionResponse;
import com.framework.service.ComponentService;
import com.framework.service.ServiceType;
import com.framework.utils.Configuration;
import com.framework.utils.NamedThreadFactory;
import com.framework.utils.PropertiesUtils;
import com.framework.utils.rmcontext.RMContext;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

public class ApiManager extends ComponentService implements HttpRequestHandler {

    private static Logger logger = Logger.getLogger(ApiManager.class);

    private static ExecutorService executor;

    private static int workerCount = 0;

    private static int apiPort;

    private static String contentType = "application/json";

    private static Map<String, Class<? extends Message>> messageClazzMap =
            new HashMap<String, Class<? extends Message>>();

    private static Thread listenerThread = null;
    
    public ApiManager() {
        super(ServiceType.ApiManager.toString());
    }

    @Override
    public Class<?> getMessageType() {
        return null;
    }

    @Override
    protected void serviceInit(Configuration conf) throws Exception {
        logger.info("initing service " + this.getName() + " ...");
        super.serviceInit(conf);
        executor =
                new ThreadPoolExecutor(conf.MIN_API_THREAD_POOL_SIZE,
                        conf.MAX_CONCURRENT_API_REQUEST, conf.API_THREAD_KEEP_ALIVE_TIME,
                        TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                        new NamedThreadFactory("ApiServer"));
        apiPort = Integer.valueOf(PropertiesUtils.getConfig("api.service.port", "8080"));

        Reflections reflections = new Reflections(ClasspathHelper.forPackage("com.framework"),
                new TypeAnnotationsScanner());

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Api.class);
        for(Class clz: classes) {
            Api at = (Api) clz.getAnnotation(Api.class);
            String action = at.action();
            messageClazzMap.put(action, clz);
        }
    }

    @Override
    protected void serviceStart() throws Exception {
        logger.info("starting service " + this.getName() + " ...");
        super.serviceStart();
        listenerThread = new ListenerThread(this, apiPort);
        listenerThread.start();
    }

    @Override
    protected void serviceStop() throws Exception {
        logger.info("stopping service " + this.getName() + " ...");
        listenerThread.interrupt();
        executor.shutdownNow();
        super.serviceStop();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    static class ListenerThread extends Thread {
        private HttpService httpService = null;
        private ServerSocket serverSocket = null;
        private HttpParams params = null;

        @SuppressWarnings("deprecation")
        public ListenerThread(ApiManager requestHandler, int port) {
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException ioex) {
                logger.error("error initializing api server", ioex);
                return;
            }

            params = new BasicHttpParams();
            params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 30000)
                    .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                    .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                    .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                    .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");

            // Set up the HTTP protocol processor
            BasicHttpProcessor httpproc = new BasicHttpProcessor();
            httpproc.addInterceptor(new ResponseDate());
            httpproc.addInterceptor(new ResponseServer());
            httpproc.addInterceptor(new ResponseContent());
            httpproc.addInterceptor(new ResponseConnControl());

            // Set up request handlers
            HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
            reqistry.register("/api*", requestHandler);

            // Set up the HTTP service
            httpService =
                    new HttpService(httpproc, new NoConnectionReuseStrategy(),
                            new DefaultHttpResponseFactory());
            httpService.setParams(params);
            httpService.setHandlerResolver(reqistry);
        }

        @Override
        public void run() {
            logger.info("ApiServer listening on port " + apiPort);
            while (!Thread.interrupted()) {
                try {
                    // Set up HTTP connection
                    Socket socket = serverSocket.accept();
                    DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                    conn.bind(socket, params);

                    // Execute a new worker task to handle the request
                    executor.execute(new WorkerTask(httpService, conn, workerCount++));
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    logger.error("I/O error initializing connection thread", e);
                    break;
                }
            }
        }
    }

    static class WorkerTask implements Runnable {
        private final HttpService httpService;
        private final HttpServerConnection conn;
        private final int count;

        public WorkerTask(final HttpService httpService, final HttpServerConnection conn,
                final int count) {
            this.httpService = httpService;
            this.conn = conn;
            this.count = count;
        }

        public void run() {
            if (logger.isTraceEnabled())
                logger.trace("ApiServer has handled " + count + " requests since started.");
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && conn.isOpen()) {
                    httpService.handleRequest(conn, context);
                    conn.close();
                }
            } catch (ConnectionClosedException ex) {
                if (logger.isTraceEnabled()) {
                    logger.trace("ApiServer:  Client closed connection");
                }
            } catch (IOException ex) {
                if (logger.isTraceEnabled()) {
                    logger.trace("ApiServer:  IOException - " + ex);
                }
            } catch (HttpException ex) {
                logger.warn("ApiServer:  Unrecoverable HTTP protocol violation" + ex);
            } finally {
                try {
                    conn.shutdown();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        StringBuilder audit =
                new StringBuilder("--apiserver got a request " + request.getRequestLine()
                        + " from remote addr ");
        HttpServerConnection connObj =
                (HttpServerConnection) context.getAttribute("http.connection");
        if (connObj instanceof SocketHttpServerConnection) {
            InetAddress remoteAddr = ((SocketHttpServerConnection) connObj).getRemoteAddress();
            audit.append(remoteAddr.toString() + " -- ");
        }
        try {
            if (!"GET".equals(request.getRequestLine().getMethod().toUpperCase()))
                throw new HttpRequestException(HttpStatus.SC_NOT_IMPLEMENTED,
                        "unsupported request method " + request.getRequestLine().getMethod());
            List<NameValuePair> paramList = null;
            try {
                paramList =
                        URLEncodedUtils.parse(new URI(request.getRequestLine().getUri()), "UTF-8");
            } catch (URISyntaxException e) {
                logger.error("Error parsing url request", e);
            }
            String action = null;
            Map<String, String> params = new HashMap<String, String>();
            for (NameValuePair param : paramList) {
                if ("action".equals(param.getName().toLowerCase()))
                    action = param.getValue();
                params.put(param.getName().toLowerCase(), param.getValue());
            }

            if (action == null)
                throw new HttpRequestException(HttpStatus.SC_BAD_REQUEST, "no action in params!");
            if (messageClazzMap.get(action) == null)
                throw new HttpRequestException(HttpStatus.SC_NOT_FOUND, "unsupported action:"
                        + action);
            Message message =
                    ApiMessageBuilder
                            .processParameters(action, messageClazzMap.get(action), params);
            Object responseText = handleMessage(message, action);
            audit.append("\n response is " + responseText + "\t status code 200.");
            writeResponse((String) responseText, HttpStatus.SC_OK, response);
        } catch (HttpRequestException e) {
            String responseText = e.getMessage();
            audit.append("\n exception response is: " + responseText + "\t status code:"
                    + e.getStatusCode());
            ExceptionResponse r =
                    new ExceptionResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, responseText);
            writeResponse(MessageBuilder.dumpResponse(r), e.getStatusCode(), response);
        } catch (BaseException e) {
            String responseText = e.getMessage();
            audit.append("\n exception response is: " + responseText + "\t errorcode "
                    + e.getErrorCode());
            ExceptionResponse r =
                    new ExceptionResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, responseText);
            writeResponse(MessageBuilder.dumpResponse(r), HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    response);
        } catch (BaseRuntimeException e) {
            String responseText = e.getMessage();
            audit.append("\n exception response is: " + responseText);
            ExceptionResponse r =
                    new ExceptionResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, responseText);
            writeResponse(MessageBuilder.dumpResponse(r), HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    response);
        } catch (RuntimeException e) {
            logger.error("unhandled exception " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("unhandled exception " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            logger.info(audit);
        }
    }

    @SuppressWarnings("unchecked")
    private Object handleMessage(Message message, String action) throws BaseException {
        BaseResponse result =
                (BaseResponse) RMContext.getDispatcher().getMessageHandler().handle(message);
        return MessageBuilder.dumpResponse(result, action);
    }

    public static void writeResponse(String responseText, int statusCode, HttpResponse resp) {
        try {
            resp.setStatusCode(statusCode);

            BasicHttpEntity body = new BasicHttpEntity();
            body.setContentType(contentType + ";charset=UTF-8");
            if (responseText == null) {
                body.setContent(new ByteArrayInputStream(
                        "{ \"error\" : { \"description\" : \"Internal Server Error\" } }"
                                .getBytes("UTF-8")));
            } else {
                body.setContent(new ByteArrayInputStream(responseText.getBytes("UTF-8")));
            }
            resp.setEntity(body);
        } catch (Exception ex) {
            logger.error("error!", ex);
        }
    }

    public static Map<String, Class<? extends Message>> getMessageClazzMap() {
        return messageClazzMap;
    }

    public static void setMessageClazzMap(Map<String, Class<? extends Message>> messageClazzMap) {
        ApiManager.messageClazzMap = messageClazzMap;
    }
}
