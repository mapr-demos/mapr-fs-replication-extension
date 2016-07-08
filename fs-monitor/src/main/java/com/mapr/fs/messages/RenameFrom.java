package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

/**
 * One of a pair of events that signify a file renaming.
 */
public class RenameFrom implements Message {
    @JsonProperty("type")
    public final String type = "rename_from";

    @JsonProperty("oldName")
    String oldName;
    @JsonProperty("newName")
    String newName;

    public RenameFrom() {
    }

    public RenameFrom(Path oldName, Path newName) {
        this.oldName = oldName.toString();
        this.newName = newName.toString();
    }
}
