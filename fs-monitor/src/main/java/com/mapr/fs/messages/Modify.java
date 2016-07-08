package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;
import java.util.List;

/**
 * Show that a file has been changed
 */
public class Modify implements Message {
    @JsonProperty("type")
    public final String type = "modify";

    @JsonProperty("name")
    public String name;
    @JsonProperty("size")
    public Long fileSize;
    @JsonProperty("changes")
    public List<Long> changedBlocks;
    @JsonProperty("changesContent")
    public List<String> changedBlocksContent;

    public Modify() {
    }

    public Modify(Path name, Long fileSize, List<Long> changedBlocks, List<String> changedBlocksContent) {
        this.name = name.toString();
        this.fileSize = fileSize;
        this.changedBlocks = changedBlocks;
        this.changedBlocksContent = changedBlocksContent;
    }
}
