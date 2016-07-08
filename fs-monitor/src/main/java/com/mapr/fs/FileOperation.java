package com.mapr.fs;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.List;

/**
 * Represents a single file system operation. This can be a creation, deletion, modification or rename.
 * Renames are not generally seen in the raw modification events, but instead appears as a delete and
 * create in close succession. As such, it is convenient to be able to "upgrade" a deletion into a
 * rename by simply adding a create event.
 */
class FileOperation {
    // how long should we wait for the second half of a rename?
    public static double maxTimeForRename = 0.1;

    // when was this even first seen? Used to decide when a delete is really just a delete rather than
    // the beginning of a rename
    double start = System.nanoTime() / 1e9;

    // these hold the primitive events for this operation. Exactly one of these should be non-null,
    // except in the case of rename when both delete and create will be non-null.
    private WatchEvent<Path> delete;
    private WatchEvent<Path> create;
    private final WatchEvent<Path> modify;
    private final List<Long> changes;
    private Path watchDir;

    private FileOperation(Path watchDir, WatchEvent<Path> delete, WatchEvent<Path> create, WatchEvent<Path> modify, List<Long> changes) {
        this.watchDir = watchDir;
        int activeCount = 0;
        activeCount += delete != null ? 1 : 0;
        activeCount += create != null ? 1 : 0;
        activeCount += modify != null ? 1 : 0;
        if (activeCount == 1) {
            this.delete = delete;
            this.create = create;
            this.modify = modify;
            if (modify != null) {
                if (changes != null) {
                    this.changes = changes;
                } else {
                    throw new IllegalArgumentException("ModifyEvent events must have associated changes");
                }
            } else {
                this.changes = null;
            }
        } else {
            throw new IllegalArgumentException("Can only be delete, create or modify at construction time");
        }
    }

    public static FileOperation delete(Path watchDir, WatchEvent<Path> event) {
        return new FileOperation(watchDir, event, null, null, null);
    }

    public static FileOperation create(Path watchDir, WatchEvent<Path> event) {
        return new FileOperation(watchDir, null, event, null, null);
    }

    public static FileOperation modify(Path watchDir, WatchEvent<Path> event, List<Long> longs) {
        return new FileOperation(watchDir, null, null, event, longs);
    }

    public void addCreate(WatchEvent<Path> event) {
        if (delete == null || modify != null || create != null) {
            throw new IllegalArgumentException("Can only add creation to delete event");
        }
        create = event;
    }

    public void addDelete(WatchEvent<Path> event) {
        if (create == null || modify != null || delete != null) {
            throw new IllegalArgumentException("Can only add deletion to creation event");
        }
        delete = event;
    }

    public Path getCreatePath() {
        return watchDir.resolve(create.context());
    }

    public Path getDeletePath() {
        return watchDir.resolve(delete.context());
    }

    public Path getModifyPath() {
        return watchDir.resolve(modify.context());
    }

    // for testing
    public static void setMaxTimeForRename(double maxTimeForRename) {
        FileOperation.maxTimeForRename = maxTimeForRename;
    }

    public boolean isRename() {
        return delete != null && create != null;
    }

    public boolean isOldDelete() {
        return delete != null && (System.nanoTime() / 1e9 - start) > maxTimeForRename;
    }

    public boolean isOldCreate() {
        return create != null && (System.nanoTime() / 1e9 - start) > maxTimeForRename;
    }

    public boolean isModify() {
        return modify != null;
    }

    public List<Long> getModifiedOffsets() {
        return changes;
    }
}
