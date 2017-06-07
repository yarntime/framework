package com.framework.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertiesUtils {

    private static final Logger logger = Logger.getLogger(PropertiesUtils.class);

    public static final String CONFIG_FILE = "/configuration.properties";

    public static Properties configProperties;

    public static File findConfigFile(String path) {

        ClassLoader cl = PropertiesUtils.class.getClassLoader();
        URL url = cl.getResource(path);
        if (url != null) {
            return new File(url.getFile());
        }

        url = ClassLoader.getSystemResource(path);
        if (url != null) {
            return new File(url.getFile());
        }

        File file = new File(path);
        if (file.exists()) {
            return file;
        }

        String newPath = path;
        url = ClassLoader.getSystemResource(newPath);
        if (url != null) {
            return new File(url.getFile());
        }

        url = cl.getResource(newPath);
        if (url != null) {
            return new File(url.getFile());
        }

        newPath = "conf" + (path.startsWith(File.separator) ? "" : File.separator) + path;
        file = new File(newPath);
        if (file.exists()) {
            return file;
        }

        newPath = System.getProperty("catalina.home");
        if (newPath == null) {
            newPath = System.getenv("CATALINA_HOME");
        }

        if (newPath == null) {
            newPath = System.getenv("CATALINA_BASE");
        }

        if (newPath == null) {
            return null;
        }

        file = new File(newPath + File.separator + "conf" + File.separator + path);
        if (file.exists()) {
            return file;
        }

        return null;

    }

    private static void loadConfig() {

        InputStream input = PropertiesUtils.class.getResourceAsStream(CONFIG_FILE);
        Properties properties = new Properties();
        try {
            properties.load(input);
            configProperties = properties;
        } catch (FileNotFoundException e) {
            logger.debug("failed to get " + CONFIG_FILE, e);
        } catch (IOException e) {
            logger.debug("failed to get " + CONFIG_FILE, e);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getConfig(String key) {
        if (configProperties == null) {
            loadConfig();
        }
        return configProperties.getProperty(key);
    }

    public static String getConfig(String key, String defaultValue) {
        if (configProperties == null || configProperties.isEmpty()) {
            loadConfig();
        }
        return configProperties.getProperty(key, defaultValue);
    }

}
