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
        if (!loadConfig()) {
            throw new RuntimeException("Config file not found");
        }
    }

    private boolean loadConfig() {
        for (String path : CONFIG_PATHS) {
            try (InputStream props = new FileInputStream(path)) {
                properties.load(props);
                System.err.println("Config found at path " + path);
                return true;
            } catch (IOException e) {
                System.err.println("Config not found at path " + path);
            }
        }
        return false;
    }

    public static synchronized Config getConfig() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public Properties getPrefixedProps(String... prefixes) {
        Properties props = new Properties();
        for (String prefix : prefixes) {
            for (final String name : properties.stringPropertyNames()) {
                if (name.startsWith(prefix)) {
                    props.put(name.substring(prefix.length()), properties.getProperty(name));
                }
            }
        }
        return props;
    }

    public String getProducerTopicName(String topic) {
        return properties.get(KAFKA_PRODUCER_STREAM) + ":" + topic;
    }
}
