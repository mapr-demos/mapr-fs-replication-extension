package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;
import java.util.List;

/**
 * Show that a file has been changed
 */
public class Change {
    @JsonProperty("type")
    public final String type = "change";

    @JsonProperty("name")
    public String name;
    @JsonProperty("changes")
    public List<Long> changedBlocks;

    public Change(Path name, List<Long> changedBlocks) {
        this.name = name.toString();
        this.changedBlocks = changedBlocks;
    }
}
