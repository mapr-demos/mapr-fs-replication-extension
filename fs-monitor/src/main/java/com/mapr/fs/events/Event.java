package com.mapr.fs.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.IOException;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateEvent.class, name = "create"),
        @JsonSubTypes.Type(value = DeleteEvent.class, name = "delete"),
        @JsonSubTypes.Type(value = ModifyEvent.class, name = "modify"),
        @JsonSubTypes.Type(value = RenameFromEvent.class, name = "rename_from"),
        @JsonSubTypes.Type(value = RenameToEvent.class, name = "rename_to")})
public interface Event {
    void execute(String volumePath) throws IOException;
}
