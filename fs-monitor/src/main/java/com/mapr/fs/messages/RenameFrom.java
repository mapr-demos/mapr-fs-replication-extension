package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

/**
 * One of a pair of events that signify a file renaming.
 */
public class RenameFrom {
    @JsonProperty("oldName")
    Path oldName;
    @JsonProperty("newName")
    Path newName;

    public RenameFrom(Path oldName, Path newName) {
        this.oldName = oldName;
        this.newName = newName;
    }
}
