package com.framework.resourcemanager;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.framework.controller.ApiManager;
import com.framework.message.DispatcherType;
import com.framework.message.MessageHandler;
import com.framework.service.ComponentService;
import com.framework.service.ServiceType;
import com.framework.utils.Configuration;

public class ResourceManager extends ComponentService implements MessageHandler<ServiceMsg> {

    private static Logger logger = Logger.getLogger(ResourceManager.class);

    private RMContext rmContext;

    private static String identification = null;
    private static String url = null;
    private static String userName = null;
    private static String password = null;
    public static Integer port = null;

    public ResourceManager() {
        super(ServiceType.ResourceManager.toString(), null);
    }

    public ResourceManager(String _identification, String _url, String _userName, String _password) {
        super(ServiceType.ResourceManager.toString(), null);
        identification = _identification;
        url = _url;
        userName = _userName;
        password = _password;
        port = Integer.valueOf(_identification);
    }

    @Override
    protected void serviceInit(Configuration conf) throws Exception {

        logger.info("initing service " + this.getName() + " ...");

        this.rmContext = new RMContextImpl();

        super.serviceInit(conf);

        registeDispatcher(this.dispatcher);

        rmContext.setDispatcher(this.dispatcher);
    }

    @Override
    protected void serviceStart() throws Exception {
        logger.info("starting service " + this.getName() + " ...");
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        logger.info("stopping service " + this.getName() + " ...");
        super.serviceStop();
    }

    public static void main(String argv[]) throws IOException {

        if (argv.length != 4) {
            logger.error("Expected --_identification, --url, --username, --password arguments. shutdown service.");
            System.exit(1);
        }

        identification = argv[0];
        url = argv[1];
        userName = argv[2];
        password = argv[3];
        port = Integer.valueOf(identification);

        try {
            Configuration conf = new Configuration();

            ResourceManager resourceManager = new ResourceManager();

            resourceManager.init(conf);
            resourceManager.start();

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public Class<?> getMessageType() {
        return ServiceMsg.class;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return DispatcherType.Sync;
    }

    @Override
    public Object handle(ServiceMsg message) {
        return null;
    }
}
