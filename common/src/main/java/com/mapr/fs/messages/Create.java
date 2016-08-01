package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.nio.file.Path;

/**
 * Indicates a file has been created.
 */
public class Create implements Message {

    private String name;
    private boolean directory;

    public Create(Path name, boolean directory) {
        this(name.toString(), directory);
    }

    @JsonCreator
    public Create(@JsonProperty("name") String name, @JsonProperty("directory") boolean directory) {
        this.name = name;
        this.directory = directory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Create create = (Create) o;
        return Objects.equal(directory, create.directory) &&
                Objects.equal(name, create.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, directory);
    }
}
