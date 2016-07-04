package com.mapr.fs.messages;

import java.nio.file.Path;

/**
 * Created by tdunning on 7/3/16.
 */
public class Create {
    public Path name;

    public Create(Path name) {
        this.name = name;
    }
}
