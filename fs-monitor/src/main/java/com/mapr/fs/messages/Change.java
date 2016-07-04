package com.mapr.fs.messages;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by tdunning on 7/3/16.
 */
public class Change {
    public Path name;
    List<Long> changedBlocks;

    public Change(Path name, List<Long> changedBlocks) {
        this.name = name;
        this.changedBlocks = changedBlocks;
    }

    @Override
    public String toString() {
        return "Change{" +
                "changedBlocks=" + changedBlocks +
                ", name=" + name +
                '}';
    }
}
