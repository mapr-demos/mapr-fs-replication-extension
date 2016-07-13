package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.nio.file.Path;
import java.util.List;

/**
 * Show that a file has been changed
 */
public class Modify implements Message {
    public String name;
    public Long fileSize;
    public List<Long> changedBlocks;
    public List<String> changedBlocksContent;

    public Modify(Path name, Long fileSize, List<Long> changedBlocks, List<String> changedBlocksContent) {
        this(name.toString(), fileSize, changedBlocks, changedBlocksContent);
    }

    public Modify(@JsonProperty("name") String name,
                  @JsonProperty("size") Long fileSize,
                  @JsonProperty("changes") List<Long> changedBlocks,
                  @JsonProperty("changesContent") List<String> changedBlocksContent) {
        this.name = name;
        this.fileSize = fileSize;
        this.changedBlocks = changedBlocks;
        this.changedBlocksContent = changedBlocksContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Modify modify = (Modify) o;
        return Objects.equal(name, modify.name) &&
                Objects.equal(fileSize, modify.fileSize) &&
                Objects.equal(changedBlocks, modify.changedBlocks) &&
                Objects.equal(changedBlocksContent, modify.changedBlocksContent);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, fileSize, changedBlocks, changedBlocksContent);
    }
}
