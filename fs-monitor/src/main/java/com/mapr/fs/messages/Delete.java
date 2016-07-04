package com.mapr.fs.messages;

import java.nio.file.Path;

/**
 * Created by tdunning on 7/3/16.
 */
public class Delete {
    public Path name;

    public Delete(Path name) {
        this.name = name;
    }
}
