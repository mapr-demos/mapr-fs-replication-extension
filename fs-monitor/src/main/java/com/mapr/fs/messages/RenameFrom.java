package com.mapr.fs.messages;

import java.nio.file.Path;

/**
 * Created by tdunning on 7/3/16.
 */
public class RenameFrom {
    Path oldName;
    Path newName;

    public RenameFrom(Path oldName, Path newName) {
        this.oldName = oldName;
        this.newName = newName;
    }
}
