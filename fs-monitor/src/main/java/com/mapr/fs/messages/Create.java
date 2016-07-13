package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.nio.file.Path;

/**
 * Indicates a file has been created.
 */
public class Create implements Message {
    public String name;
    public boolean directory;

    public Create(Path name, boolean directory) {
        this(name.toString(), directory);
    }

    public Create(@JsonProperty("name") String name, @JsonProperty("directory") boolean directory) {
        this.name = name;
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
