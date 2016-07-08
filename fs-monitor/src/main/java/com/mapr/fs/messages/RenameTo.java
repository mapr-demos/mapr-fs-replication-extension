package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

/**
 * One of a pair of events that describes a renaming
 */
public class RenameTo implements Message {
    @JsonProperty("type")
    public final String type = "rename_to";

    @JsonProperty("oldName")
    public String oldName;
    @JsonProperty("newName")
    public String newName;

    public RenameTo() {
    }

    public RenameTo(Path oldName, Path newName) {
        this.oldName = oldName.toString();
        this.newName = newName.toString();
    }
}
