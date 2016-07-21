package com.mapr.fs.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.fs.Util;

import java.io.IOException;
import java.util.Map;

public class EventFactory {
    private final ObjectMapper mapper = Util.getObjectMapper();

    public Event parseEvent(String data) throws IOException {
        return mapper.readValue(data, Event.class);
    }

    private String getType(String data) throws IOException {
        @SuppressWarnings("unchecked") Map<String, Object> jsonData = mapper.readValue(data, Map.class);
        return jsonData.get("type").toString();
    }
}
