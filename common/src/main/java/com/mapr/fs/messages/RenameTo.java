package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.nio.file.Path;

/**
 * One of a pair of events that describes a renaming
 */
public class RenameTo implements Message {
    private String oldName;
    private String newName;

    public RenameTo(Path oldName, Path newName) {
        this(oldName.toString(), newName.toString());
    }

    @JsonCreator
    public RenameTo(@JsonProperty("oldName") String oldName, @JsonProperty("newName") String newName) {
        this.oldName = oldName;
        this.newName = newName;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RenameTo renameTo = (RenameTo) o;
        return Objects.equal(oldName, renameTo.oldName) &&
                Objects.equal(newName, renameTo.newName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(oldName, newName);
    }
}
