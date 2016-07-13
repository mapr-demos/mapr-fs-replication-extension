package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

/**
 * One of a pair of events that signify a file renaming.
 */
public class RenameFrom implements Message {
    @JsonProperty("oldName")
    public String oldName;
    @JsonProperty("newName")
    public String newName;

    public RenameFrom() {
    }

    public RenameFrom(Path oldName, Path newName) {
        this.oldName = oldName.toString();
        this.newName = newName.toString();
    }
}
