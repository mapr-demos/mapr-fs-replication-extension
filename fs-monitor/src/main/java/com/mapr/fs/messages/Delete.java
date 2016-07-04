package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

/**
 * Indicates a file has been deleted.
 */
public class Delete {
    @JsonProperty("name")
    public Path name;

    public Delete(Path name) {
        this.name = name;
    }
}
