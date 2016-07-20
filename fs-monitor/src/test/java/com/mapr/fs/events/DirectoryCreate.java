package com.mapr.fs.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.io.IOException;

/**
 * Records creation of a directory
 */
public class DirectoryCreate extends RealEvent {
    @JsonProperty("name")
    public String name;

    @Override
    public void doit() throws IOException {
        File f = new File(name);
        if (!f.mkdirs()) {
            throw new IOException("Mkdir failed for " + name);
        }
    }
}
