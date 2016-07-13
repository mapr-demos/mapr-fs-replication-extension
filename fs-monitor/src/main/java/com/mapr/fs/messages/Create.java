package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

/**
 * Indicates a file has been created.
 */
public class Create implements Message {
    @JsonProperty("name")
    public String name;

    @JsonProperty("directory")
    public boolean directory;

    public Create() {
    }

    public Create(Path name, boolean directory) {
        this.name = name.toString();
        this.directory = directory;
    }
}
