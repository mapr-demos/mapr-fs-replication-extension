package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

/**
 * Indicates a file has been deleted.
 */
public class Delete implements Message {
    @JsonProperty("name")
    public String name;

    public Delete() {
    }

    public Delete(Path name) {
        this.name = name.toString();
    }
}
