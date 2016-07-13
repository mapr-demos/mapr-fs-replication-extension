package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Create.class, name = "create"),
        @JsonSubTypes.Type(value = Delete.class, name = "delete"),
        @JsonSubTypes.Type(value = Modify.class, name = "modify"),
        @JsonSubTypes.Type(value = RenameFrom.class, name = "rename_from"),
        @JsonSubTypes.Type(value = RenameTo.class, name = "rename_to")
})
public interface Message {
}
