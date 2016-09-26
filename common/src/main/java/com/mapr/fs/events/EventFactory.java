package com.mapr.fs.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.fs.Util;
import com.mapr.fs.messages.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class EventFactory {

    private final ObjectMapper mapper = Util.getObjectMapper();

    public Event parseEvent(String data) throws IOException {
        Message message = mapper.readValue(data, Message.class);
        return getEvent(message);
    }

    private String getType(String data) throws IOException {
        @SuppressWarnings("unchecked") Map<String, Object> jsonData = mapper.readValue(data, Map.class);
        return jsonData.get("type").toString();
    }

    private Event getEvent(Message message) throws IOException {
        if (message instanceof Create) {
            return new CreateEvent((Create) message);
        } else if (message instanceof Delete) {
            return new DeleteEvent((Delete) message);
        } else if (message instanceof Modify) {
            return new ModifyEvent((Modify) message);
        } else if (message instanceof RenameFrom) {
            return new RenameFromEvent((RenameFrom) message);
        } else if (message instanceof RenameTo) {
            return new RenameToEvent((RenameTo) message);
        } else {
            throw new IllegalArgumentException("Undefined type of message");
        }
    }
}