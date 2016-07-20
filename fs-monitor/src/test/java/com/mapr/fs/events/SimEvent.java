package com.mapr.fs.events;

/**
 * Created by tdunning on 7/19/16.
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FileWrite.class, name = "write"),
        @JsonSubTypes.Type(value = DirectoryCreate.class, name = "mkdir"),
        @JsonSubTypes.Type(value = FileRename.class, name = "rename"),
        @JsonSubTypes.Type(value = FileRename.class, name = "verify"),
        @JsonSubTypes.Type(value = FileOperationNotification.class, name = "watch-event")
})
public class SimEvent {
    @JsonProperty("time")
    public double time = 0;
}
