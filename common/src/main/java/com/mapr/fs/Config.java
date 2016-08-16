package com.mapr.fs;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static final Logger log = Logger.getLogger(Config.class);


    private static final String CONFIG_PATHS[] = {"/tmp/config.conf", "config.conf"};
    private static final String KAFKA_STREAM = "stream";
    private static final String MONITOR_TOPIC = "change_%s";
    public static final String APPS_DIR = new Config("cluster.").getProperties().getProperty("database");

    private Properties properties = new Properties();

    public Config(String path, String[] prefixes) {
        Properties configProps = loadConfig(path);

        if (properties == null) {
            log.error("Configuration file not found");
            throw new RuntimeException("Configuration file not found");
        }

        fillPropertiesWithPrefixes(configProps, prefixes);
    }

    public Config(String... prefixes) {
        Properties configProps = loadConfig();

        if (properties == null) {
            log.error("Configuration file not found");
            throw new RuntimeException("Configuration file not found");
        }

        fillPropertiesWithPrefixes(configProps, prefixes);
    }

    private void fillPropertiesWithPrefixes(Properties configProps, String[] prefixes) {
        for (String prefix : prefixes) {
            for (final String name : configProps.stringPropertyNames()) {
                if (name.startsWith(prefix)) {
                    properties.put(name.substring(prefix.length()), configProps.getProperty(name));
                }
            }
        }
    }

    private Properties loadConfig() {
        Properties properties = null;
        for (String path : CONFIG_PATHS) {
            properties = loadConfig(path);
            if (properties != null) {
                break;
            }
        }
        return properties;
    }

    private Properties loadConfig(String path) {
        try (InputStream props = new FileInputStream(path)) {
            Properties properties = new Properties();
            properties.load(props);
            log.info("Configuration file found at path " + path);
            return properties;
        } catch (IOException e) {
            log.error("Configuration file not found at path "+ path);
            throw new RuntimeException( "Configuration file not found at path "+ path );
        }
    }

    public String getTopicName(String topic) {
        return properties.get(KAFKA_STREAM) + ":" + topic;
    }

    public Properties getProperties() {
        return properties;
    }

    public static String getMonitorTopic(String volumeName) {
        return String.format(Config.MONITOR_TOPIC, volumeName);
    }
}
