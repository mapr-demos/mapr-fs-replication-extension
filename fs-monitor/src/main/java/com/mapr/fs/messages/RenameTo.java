package com.mapr.fs.messages;

import java.nio.file.Path;

/**
 * Created by tdunning on 7/3/16.
 */
public class RenameTo {
    public Path oldName;
    public Path newName;

    public RenameTo(Path oldName, Path newName) {
        this.oldName = oldName;
        this.newName = newName;
    }
}
