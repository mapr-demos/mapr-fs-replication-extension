package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

/**
 * One of a pair of events that describes a renaming
 */
public class RenameTo {
    @JsonProperty("oldName")
    public Path oldName;
    @JsonProperty("newName")
    public Path newName;

    public RenameTo(Path oldName, Path newName) {
        this.oldName = oldName;
        this.newName = newName;
    }
}
