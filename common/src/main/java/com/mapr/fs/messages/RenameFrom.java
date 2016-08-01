package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.nio.file.Path;

/**
 * One of a pair of events that signify a file renaming.
 */
public class RenameFrom implements Message {
    private String oldName;
    private String newName;

    public RenameFrom(Path oldName, Path newName) {
        this(oldName.toString(), newName.toString());
    }

    @JsonCreator
    public RenameFrom(@JsonProperty("oldName") String oldName, @JsonProperty("newName") String newName) {
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
        RenameFrom that = (RenameFrom) o;
        return Objects.equal(oldName, that.oldName) &&
                Objects.equal(newName, that.newName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(oldName, newName);
    }
}
