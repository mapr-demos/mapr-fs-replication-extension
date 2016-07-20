package com.mapr.fs.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Records the renaming of a file or directory
 */
public class FileRename extends RealEvent {
    @JsonProperty("from")
    public String from;
    @JsonProperty("to")
    public String to;

    @Override
    public void doit() throws IOException {
        File fromFile = new File(from);
        if (fromFile.exists()) {
            boolean r = fromFile.renameTo(new File(to));
            if (!r) {
                throw new IOException("Rename from " + from + " to " + to + " failed for unknown reason");
            }
        } else {
            throw new FileNotFoundException("No such file: " + from);
        }
    }
}

