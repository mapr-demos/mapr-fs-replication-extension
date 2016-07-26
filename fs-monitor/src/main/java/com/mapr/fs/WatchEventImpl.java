package com.mapr.fs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * Json-able version of the events that the FileWatcher gives us.
 */
public class WatchEventImpl implements WatchEvent<Path> {
    private Kind<Path> kind;
    private Path context;
    private int count = 1;

    WatchEventImpl(Kind<Path> kind, Path context) {
        this.kind = kind;
        this.context = context;
        this.count = 1;
    }

    public Kind<Path> kind() {
        return this.kind;
    }

    public Path context() {
        return this.context;
    }

    public int count() {
        return this.count;
    }

    void increment() {
        ++this.count;
    }

    @JsonProperty("context")
    public void setContext(String context) {
        this.context = new File(context).toPath();
    }

    @JsonProperty("kind")
    public void setKind(String kind) {
        switch (kind) {
            case "delete":
                this.kind = StandardWatchEventKinds.ENTRY_DELETE;
                break;
            case "create":
                this.kind = StandardWatchEventKinds.ENTRY_CREATE;
                break;
            case "modify":
                this.kind = StandardWatchEventKinds.ENTRY_MODIFY;
                break;
            default:
                throw new IllegalArgumentException("Don't understand kind: " + kind);
        }
    }

    @JsonProperty("count")
    public void setCount(int count) {
        this.count = count;
    }

    static WatchEventImpl delete(Path f) {
        return new WatchEventImpl(StandardWatchEventKinds.ENTRY_DELETE, f);
    }

    static WatchEventImpl create(Path f) {
        return new WatchEventImpl(StandardWatchEventKinds.ENTRY_CREATE, f);
    }

    static WatchEventImpl modify(Path f) {
        return new WatchEventImpl(StandardWatchEventKinds.ENTRY_MODIFY, f);
    }
}

