package com.mapr.fs.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.fs.Util;
import com.mapr.fs.messages.*;

import java.io.IOException;
import java.util.Map;

public class EventFactory {
    private final ObjectMapper mapper = Util.getObjectMapper();

    public Event parseEvent(String data) throws IOException {

        String type = getType(data);

        if(type.equalsIgnoreCase("create")){
            return new CreateEvent(mapper.readValue(data, Create.class));

        } else if(type.equalsIgnoreCase("delete")){
            return new DeleteEvent(mapper.readValue(data, Delete.class));

        } else if(type.equalsIgnoreCase("modify")){
            return new ModifyEvent(mapper.readValue(data, Modify.class));

        } else if(type.equalsIgnoreCase("rename_to")) {
            return new RenameToEvent(mapper.readValue(data, RenameTo.class));

        } else if(type.equalsIgnoreCase("rename_from")) {
            return new RenameFromEvent(mapper.readValue(data, RenameFrom.class));
        }

        return null;
    }

    private String getType(String data) throws IOException {
        @SuppressWarnings("unchecked") Map<String, Object> jsonData = mapper.readValue(data, Map.class);
        return jsonData.get("type").toString();
    }
}
