package com.mapr.fs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import com.mapr.fs.messages.*;


import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Watches a directory for changes and writes those changes as appropriate
 * to a MapR stream.
 */
public class Monitor {
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private Path root;
    private OrderingRule order;
    private Queue<FileOperation> changeBuffer;
    private Map<Object, FileOperation> changeMap;
    private final String volumeName;

    public enum OrderingRule {
        VOLUME {
            public String messageKey(Path root, Path file) {
                return root.toString();
            }
        },
        DIRECTORY {
            public String messageKey(Path root, Path file) {
                return file.getParent().toString();
            }
        },
        FILE {
            public String messageKey(Path root, Path file) {
                return file.toString();
            }
        };

        public abstract String messageKey(Path root, Path file);
    }

    private void watch(Path dir) throws IOException {
        System.out.println("Watching initiated");
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                keys.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });
        System.out.println("File tree built");
    }

    Map<Path, FileState> state = Maps.newHashMap();
    Map<Path, Object> inodes = Maps.newHashMap();

    /**
     * Creates a WatchService and registers the given directory
     */
    Monitor(String volumeName, Path dir, OrderingRule order) throws IOException {
        this.volumeName = volumeName;
        System.out.println(volumeName);
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        changeBuffer = new LinkedList<>();
        changeMap = new HashMap<>();

        this.root = dir;
        this.order = order;
        watch(this.root);
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() throws IOException {
        changeBuffer = new LinkedList<>();
        changeMap = new HashMap<>();

        JsonProducer producer = new JsonProducer("kafka.producer.", "kafka.common.");

        while (true) {
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // ignore overflow events (I don't understand them)
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                //noinspection unchecked
                bufferEvent(dir, (WatchEvent<Path>) event);
                processBufferedEvents(producer);


                // if directory is created, watch it, too
                if ((kind == ENTRY_CREATE)) {
                    try {
                        //noinspection unchecked
                        Path child = dir.resolve(((WatchEvent<Path>) event).context());
                        if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                            watch(child);
                        }
                    } catch (IOException x) {
                        // ignored
                    }
                }
            }

            if (!key.reset()) {
                keys.remove(key);

                // nothing left to watch!
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    /**
     * Adds a single event to the correct queues, merging to an existing event if appropriate.
     *
     * Exposed for testing only.
     *
     *
     * @param watchDir
     * @param event The event to merge
     * @throws IOException If we can't stat the file to get a unique key
     */
    void bufferEvent(Path watchDir, WatchEvent<Path> event) throws IOException {
        if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
            processDeleteEvent(watchDir, event);
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
            processCreateEvent(watchDir, event);
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
            processModifyEvent(watchDir, event);
        }
    }

    private void processModifyEvent(Path watchDir, WatchEvent<Path> event) {
        Path filePath = watchDir.resolve(event.context());
        System.out.println(ENTRY_MODIFY);
        System.out.println(filePath);

        // emit any buffered changes
        try {
            FileState oldState = state.get(filePath);
            FileState newState = FileState.getFileInfo(filePath);
            changeBuffer.add(FileOperation.modify(watchDir, event, newState.changedBlockOffsets(oldState)));
            state.put(filePath, newState);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void processCreateEvent(Path watchDir, WatchEvent<Path> event) throws IOException {
        System.out.println(ENTRY_CREATE);
        // check buffer in case this is the second half of a rename
        Path filePath = watchDir.resolve(event.context());
        Object k = FileState.fileKey(filePath);
        inodes.put(filePath, k);

        FileOperation op = changeMap.get(k);
        if (op != null) {
            // this is the second part of the rename
            op.addCreate(event);
            inodes.remove(op.getDeletePath());
            FileState fs = state.remove(op.getDeletePath());
            changeMap.remove(k);
            state.put(filePath, fs);
        } else {
            // this is a stand-alone creation
            changeBuffer.add(FileOperation.create(watchDir, event));
        }
    }

    private void processDeleteEvent(Path watchDir, WatchEvent<Path> event) {
        Path filePath = watchDir.resolve(event.context());
        System.out.println(ENTRY_DELETE);
        // buffer in case of a rename
        FileOperation op = FileOperation.delete(watchDir, event);
        changeBuffer.add(op);

        // also record pointer back to this op so that a later create can be added
        Object key = inodes.get(filePath);
        if (key != null) {
            changeMap.put(key, op);
        }
    }

    /**
     * Processes events in the order they were buffered, but only if they are ready.
     * The only things that can be not ready are deletes or creates that are waiting
     * to be promoted into rename events.
     *
     * Exposed for testing only.
     *
     * @param producer     Where to send the events
     * @throws IOException If we can't read the file contents to find changes or we can't
     *                     send the message.
     */
    void processBufferedEvents(JsonProducer producer) throws IOException {
        // process any events that we can process
        while (changeBuffer.size() > 0) {
            FileOperation op = changeBuffer.peek();
            if (op.isRename()) {
                emitRename(producer, op.getDeletePath(), op.getCreatePath());
                // TODO need to check here for changes just before the rename that might have been missed
            } else if (op.isOldDelete()) {
                System.out.printf("%.3f s old\n", System.nanoTime() / 1e9 - op.start);

                emitDelete(producer, op.getDeletePath());
            } else if (op.isOldCreate()) {
                emitCreate(producer, op.getCreatePath());
            } else if (op.isModify()) {
                // handling changes is a bit tricky because the file may have been deleted
                // by the time we come a' knocking
                Path changed = op.getModifyPath();
                FileState newState = FileState.getFileInfo(changed);
                if (newState != null) {
                    emitModify(producer, changed, newState.getFileSize(), op.getModifiedOffsets(),
                            newState.changedBlockContentEncoded(op.getModifiedOffsets()));
                    state.put(changed, newState);
                } else {
                    // if file was deleted before we saw the change,
                    // we just forget about it and any changes that might have happened
                    // just before it disappeared. We shouldn't emit the delete event here
                    // since it will be coming shortly
                    state.remove(changed);
                }
            } else {
                // We can only process the leading elements of the queue.
                // This keeps things in proper order. The head of the queue
                // might be a young delete or create waiting for a promotion
                // to be a rename. We should not deal with those right now.
                // Either that promotion will happen, or the event will age
                // very quickly. In either case, the blockage won't last long.
                break;
            }
            changeBuffer.remove();
        }
    }

    private void emitModify(JsonProducer producer, Path name, Long size, List<Long> fileState, List<String> changes) throws JsonProcessingException {
        producer.send(String.format(Config.MONITOR_TOPIC, volumeName), order.messageKey(root, name),
                new Change(root.relativize(name), size,
                        fileState, changes));
        System.out.println("send to stream -> MODIFY");
    }

    private void emitCreate(JsonProducer producer, Path name) throws JsonProcessingException {
        producer.send(String.format(Config.MONITOR_TOPIC, volumeName), order.messageKey(root, name),
                new Create(root.relativize(name), name.toFile().isDirectory()));
        System.out.println("send to stream -> CREATE");
    }

    private void emitDelete(JsonProducer producer, Path name) throws JsonProcessingException {
        producer.send(String.format(Config.MONITOR_TOPIC, volumeName), order.messageKey(root, name),
                new Delete(root.relativize(name)));
        System.out.println("send to stream -> DELETE");
    }

    private void emitRename(JsonProducer producer, Path oldName, Path newName) throws JsonProcessingException {
        producer.send(String.format(Config.MONITOR_TOPIC, volumeName), order.messageKey(root, oldName),
                new RenameFrom(root.relativize(oldName), root.relativize(newName)));
        producer.send(String.format(Config.MONITOR_TOPIC, volumeName), order.messageKey(root, newName),
                new RenameTo(root.relativize(oldName), root.relativize(newName)));
        System.out.println("send to stream -> RENAME");
    }

    /**
     * Exposed for testing purposes.
     * @return A reference to the internal change buffer.
     */
    Queue<FileOperation> getChangeBuffer() {
        return changeBuffer;
    }

    public void recordFileState(Path f) throws IOException {
        state.put(f, FileState.getFileInfo(f));
    }

    static void usage() {
        System.err.println("usage: java WatchDir dir");
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            usage();
        }
        Map<String, String> map = new HashMap<>();


        for (String val : args) {
            String[] arr = val.split(":");

            if (!map.containsKey(arr[0]))
                map.put(arr[0], arr[1]);
            else {
                throw new RuntimeException("Try to add existed volume");
            }
        }


        // registerDirectory directory and process its events
        for (Map.Entry<String, String> pair : map.entrySet()) {
            new Thread(() -> {
                Path dir = Paths.get(pair.getValue());
                try {
                    new Monitor(pair.getKey(), dir, OrderingRule.VOLUME).processEvents();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
