package com.mapr.fs.events;

/**
 * SimEvents are the basic element of test scripts. They can record either a real
 * filel system modification or an event arriving from a FileWatcher. This allows
 * difficult to produce race conditions to be scripted.
 *
 * The JSON form for each of these events must have a field "type" which specifies
 * what kind of event we are seeing. A time field is optional and if present will
 * control how quickly events are replayed.  Without a time field, events are played
 * back at maximum speed.
 *
 * Other fields may be present according to the specific event type.
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
