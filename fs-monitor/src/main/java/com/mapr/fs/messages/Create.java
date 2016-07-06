package com.mapr.fs.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

/**
 * Indicates a file has been created.
 */
public class Create {
    @JsonProperty("type")
    public final String type = "create";

    @JsonProperty("name")
    public String name;

    @JsonProperty("directory")
    public boolean directory;

    public Create(Path name, boolean directory) {
        this.name = name.toString();
        this.directory = directory;
    }
}
