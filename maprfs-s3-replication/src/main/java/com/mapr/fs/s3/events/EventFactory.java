package com.mapr.fs.s3.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.fs.Util;
import com.mapr.fs.events.Event;
import com.mapr.fs.messages.*;
import com.mapr.fs.PluginConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class EventFactory {
    private static final Logger log = LoggerFactory.getLogger(EventFactory.class);

    private final ObjectMapper mapper = Util.getObjectMapper();
    private final PluginConfiguration pluginConfiguration;

    public EventFactory(PluginConfiguration pluginConfiguration) {
        this.pluginConfiguration = pluginConfiguration;
    }


    public Event parseEvent(String data) throws IOException {
        Message message = mapper.readValue(data, Message.class);
        return getEvent(message);
    }

    private String getType(String data) throws IOException {
        @SuppressWarnings("unchecked") Map<String, Object> jsonData = mapper.readValue(data, Map.class);
        return jsonData.get("type").toString();
    }

    /**
     * Create and return a Event based on it type (create, delete, ...) and its configuration
     *
     * @param message
     * @return
     * @throws IOException
     */
    private Event getEvent(Message message) throws IOException {
        if (message instanceof Create) {
            if (pluginConfiguration.isCreateEnabled()) {
                return new CreateEvent(message,
                        pluginConfiguration.getBucketName(),
                        pluginConfiguration.getAccessKey(),
                        pluginConfiguration.getSecretKey()
                );
            } else {
                log.info("Create is disabled");
                return null;
            }
        } else if (message instanceof Delete) {
            if (pluginConfiguration.isDeleteEnabled()) {
                return new DeleteEvent(message,
                        pluginConfiguration.getBucketName(),
                        pluginConfiguration.getAccessKey(),
                        pluginConfiguration.getSecretKey()
                );
            } else {
                log.info("Delete is disabled");
                return null;
            }
        } else if (message instanceof Modify) {
            if (pluginConfiguration.isModifyEnabled()) {
                return new ModifyEvent(message,
                        pluginConfiguration.getBucketName(),
                        pluginConfiguration.getAccessKey(),
                        pluginConfiguration.getSecretKey()
                );
            } else {
                log.info("Modify is disabled");
                return null;
            }
        } else if (message instanceof RenameFrom) {
            // TODO implement rename
            return null;
        } else if (message instanceof RenameTo) {
            // TODO implement rename
            return null;
        } else {
            throw new IllegalArgumentException("Undefined type of message");
        }
    }
}
