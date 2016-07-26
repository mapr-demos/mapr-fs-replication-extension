package com.mapr.fs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.List;

/**
 * Represents a single file system operation. This can be a creation, deletion, modification or rename.
 * Renames are not generally seen in the raw modification events, but instead appears as a delete and
 * create in close succession. As such, it is convenient to be able to "upgrade" a deletion into a
 * rename by simply adding a create event.
 */
@JsonIgnoreProperties({"type"})
public class FileOperation {
    // how long should we wait for the second half of a rename?
    public static double maxTimeForRename = 0.1;

    // when was this even first seen? Used to decide when a delete is really just a delete rather than
    // the beginning of a rename
    @JsonProperty("time")
    public double start = System.nanoTime() / 1e9;

    // these hold the primitive events for this operation. Exactly one of these should be non-null,
    // except in the case of rename when both delete and create will be non-null.
    private WatchEvent<Path> delete;
    private WatchEvent<Path> create;
    private WatchEvent<Path> modify;
    private List<Long> changes;
    private Path watchDir;

    public FileOperation(@JsonProperty("time") double t,
                         @JsonProperty("root") String root,
                         @JsonProperty("delete_path") String delete,
                         @JsonProperty("create_path") String create,
                         @JsonProperty("modify_path") String modify,
                         @JsonProperty("changes") List<Long> changes) {

        start = t;
        watchDir = new File(root).toPath();
        if (delete != null) {
            this.delete = WatchEventImpl.delete(new File(delete).toPath());
        } else {
            this.delete = null;
        }
        if (create != null) {
            this.create = WatchEventImpl.create(new File(create).toPath());
        } else {
            this.create = null;
        }
        if (modify != null) {
            this.modify = WatchEventImpl.modify(new File(modify).toPath());
        } else {
            this.modify = null;
        }
        this.changes = changes;
    }

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

    @JsonProperty("create_path")
    public String getCreatePathName() {
        if (create != null) {
            return watchDir.relativize(create.context()).toString();
        } else {
            return null;
        }
    }

    @JsonIgnore
    public Path getCreatePath() {
        if (create != null) {
            return watchDir.resolve(create.context());
        } else {
            return null;
        }
    }

    @JsonProperty("delete_path")
    public String getDeletePathName() {
        if (delete != null) {
            return watchDir.relativize(delete.context()).toString();
        } else {
            return null;
        }
    }

    @JsonIgnore
    public Path getDeletePath() {
        if (delete != null) {
            return watchDir.resolve(delete.context());
        } else {
            return null;
        }
    }

    @JsonProperty("modify_path")
    public String getModifyPathName() {
        if (modify != null) {
            return watchDir.relativize(modify.context()).toString();
        } else {
            return null;
        }
    }

    @JsonIgnore
    public Path getModifyPath() {
        if (modify != null) {
            return watchDir.resolve(modify.context());
        } else {
            return null;
        }
    }

    // for testing
    public static void setMaxTimeForRename(double maxTimeForRename) {
        FileOperation.maxTimeForRename = maxTimeForRename;
    }

    @JsonIgnore
    public boolean isRename() {
        return delete != null && create != null;
    }

    @JsonIgnore
    public boolean isOldDelete() {
        return delete != null && (System.nanoTime() / 1e9 - start) > maxTimeForRename;
    }

    @JsonIgnore
    public boolean isOldCreate() {
        return create != null && (System.nanoTime() / 1e9 - start) > maxTimeForRename;
    }

    @JsonIgnore
    public boolean isModify() {
        return modify != null;
    }

    @JsonProperty("changes")
    public List<Long> getModifiedOffsets() {
        return changes;
    }

    @JsonProperty("root")
    public String getRoot() {
        return watchDir.toString();
    }

    @Override
    public String toString() {

        if (delete != null && create != null) {
            assert modify == null;
            return String.format("Rename in %s from %s to %s", watchDir, delete.context(), create.context());
        } else if (delete != null) {
            assert modify == null;
            return String.format("Delete in %s of %s", watchDir, delete.context());
        } else if (create != null) {
            assert modify == null;
            return String.format("Create in %s of %s", watchDir, create.context());
        } else {
            assert modify != null;
            return String.format("Modify in %s of %s", watchDir, modify.context());
        }
    }

    public void setRoot(File root) {
        this.watchDir = root.toPath();
    }

    public static FileOperation fromJson(JsonNode jsonNode) {
        return Util.getObjectMapper().convertValue(jsonNode, FileOperation.class);
    }
}
