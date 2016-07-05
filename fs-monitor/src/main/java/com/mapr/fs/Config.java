package com.mapr.fs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    public static final String CONFIG_PATHS[] = {"/tmp/config.conf", "config.conf"};
    public static final String KAFKA_PRODUCER_STREAM = "kafka.producer.stream";

    private Properties properties = new Properties();
    private static Config instance;

    private Config() {
        properties = loadConfig();
        if (properties == null) {
            throw new RuntimeException("Config file not found");
        }
    }

    public Config(String... prefixes) {
        Properties configProps = loadConfig();
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
            try (InputStream props = new FileInputStream(path)) {
                properties = new Properties();
                properties.load(props);
                System.err.println("Config found at path " + path);
                break;
            } catch (IOException e) {
                System.err.println("Config not found at path " + path);
            }
        }
        return properties;
    }

    public static synchronized Config getConfig() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public String getProducerTopicName(String topic) {
        return properties.get(KAFKA_PRODUCER_STREAM) + ":" + topic;
    }

    public Properties getProperties() {
        return properties;
    }
}
