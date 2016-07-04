package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;
import java.util.List;

/**
 * Show that a file has been changed
 */
public class Change {
    @JsonProperty("name")
    public Path name;
    @JsonProperty("changes")
    public List<Long> changedBlocks;

    public Change(Path name, List<Long> changedBlocks) {
        this.name = name;
        this.changedBlocks = changedBlocks;
    }
}
