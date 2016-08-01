package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.nio.file.Path;
import java.util.List;

/**
 * Show that a file has been changed
 */
public class Modify implements Message {
    private String name;
    private Long fileSize;
    private List<Long> changedBlocks;
    private List<String> changedBlocksContent;

    public Modify(Path name, Long fileSize, List<Long> changedBlocks, List<String> changedBlocksContent) {
        this(name.toString(), fileSize, changedBlocks, changedBlocksContent);
    }

    @JsonCreator
    public Modify(@JsonProperty("name") String name,
                  @JsonProperty("size") Long fileSize,
                  @JsonProperty("changes") List<Long> changedBlocks,
                  @JsonProperty("changesContent") List<String> changedBlocksContent) {
        this.name = name;
        this.fileSize = fileSize;
        this.changedBlocks = changedBlocks;
        this.changedBlocksContent = changedBlocksContent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public List<Long> getChangedBlocks() {
        return changedBlocks;
    }

    public void setChangedBlocks(List<Long> changedBlocks) {
        this.changedBlocks = changedBlocks;
    }

    public List<String> getChangedBlocksContent() {
        return changedBlocksContent;
    }

    public void setChangedBlocksContent(List<String> changedBlocksContent) {
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
