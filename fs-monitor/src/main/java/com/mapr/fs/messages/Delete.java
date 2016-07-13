package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.nio.file.Path;

/**
 * Indicates a file has been deleted.
 */
public class Delete implements Message {
    public String name;

    public Delete(@JsonProperty("name") String name) {
        this.name = name;
    }

    public Delete(Path name) {
        this(name.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Delete delete = (Delete) o;
        return Objects.equal(name, delete.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
