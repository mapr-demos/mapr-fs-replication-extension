package com.mapr.fs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    public static final String CONFIG_PATHS[] = {"/tmp/config.conf", "config.conf"};
    public static final String KAFKA_STREAM = "stream";
    public static final String MONITOR_TOPIC = "change_%s";

    private Properties properties = new Properties();

    public Config(String path, String[] prefixes) {
        Properties configProps = loadConfig(path);

        if (properties == null) {
            throw new RuntimeException("Config file not found");
        }

        fillPropertiesWithPrefixes(configProps, prefixes);
    }

    public Config(String... prefixes) {
        Properties configProps = loadConfig();

        if (properties == null) {
            throw new RuntimeException("Config file not found");
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
            System.err.println("Config found at path " + path);
            return properties;
        } catch (IOException e) {
            System.err.println("Config not found at path " + path);
        }
        return null;
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
