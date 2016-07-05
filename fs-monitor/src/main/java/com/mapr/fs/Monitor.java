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
    public static final String MONITOR_TOPIC = "mapr.fs.monitor";
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private Path root;
    private OrderingRule order;
    private Queue<FileOperation> changeBuffer;
    private Map<Object, FileOperation> changeMap;

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

    private void watch(Path initial, OrderingRule order) throws IOException {
        this.root = initial;
        this.order = order;
        System.out.println("Watching initiated");
        Files.walkFileTree(this.root, new SimpleFileVisitor<Path>() {
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
    Monitor(Path dir, OrderingRule order) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        changeBuffer = new LinkedList<>();
        changeMap = new HashMap<>();

        watch(dir, order);
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
                bufferEvent((WatchEvent<Path>) event);
                processBufferedEvents(producer);


                // if directory is created, watch it, too
                if ((kind == ENTRY_CREATE)) {
                    try {
                        //noinspection unchecked
                        Path child = dir.resolve(((WatchEvent<Path>) event).context());
                        if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                            watch(child, order);
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
     * @param event The event to merge
     * @throws IOException If we can't stat the file to get a unique key
     */
    void bufferEvent(WatchEvent<Path> event) throws IOException {
        if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
            // buffer in case of a rename
            FileOperation op = FileOperation.delete(event);
            changeBuffer.add(op);

            // also record pointer back to this op so that a later create can be added
            Object key = inodes.get(event.context());
            if (key != null) {
                changeMap.put(key, op);
            }
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
            // check buffer in case this is the second half of a rename
            Object k = FileState.fileKey(event.context());
            inodes.put(event.context(), k);

            FileOperation op = changeMap.get(k);
            FileState fs;
            if (op != null) {
                // this is the second part of the rename
                op.addCreate(event);
                inodes.remove(op.getDeletePath());
                fs = state.remove(op.getDeletePath());
                changeMap.remove(k);
            } else {
                // this is a stand-alone creation
                changeBuffer.add(FileOperation.create(event));
                fs = FileState.getFileInfo(event.context());
            }
            state.put(event.context(), fs);
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
            // emit any buffered changes
            FileState oldState = state.get(event.context());
            FileState newState = FileState.getFileInfo(event.context());
            changeBuffer.add(FileOperation.modify(event, newState.changedBlockOffsets(oldState)));
            state.put(event.context(), newState);

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
                    emitModify(producer, changed, op.getModifiedOffsets());
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

    private void emitModify(JsonProducer producer, Path name, List<Long> fileState) throws JsonProcessingException {
        producer.send(MONITOR_TOPIC, order.messageKey(root, name), new Change(name, fileState));
    }

    private void emitCreate(JsonProducer producer, Path name) throws JsonProcessingException {
        producer.send(MONITOR_TOPIC, order.messageKey(root, name), new Create(name));
    }

    private void emitDelete(JsonProducer producer, Path name) throws JsonProcessingException {
        producer.send(MONITOR_TOPIC, order.messageKey(root, name), new Delete(name));
    }

    private void emitRename(JsonProducer producer, Path oldName, Path newName) throws JsonProcessingException {
        producer.send(MONITOR_TOPIC, order.messageKey(root, oldName), new RenameFrom(oldName, newName));
        producer.send(MONITOR_TOPIC, order.messageKey(root, newName), new RenameTo(oldName, newName));
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
        if (args.length == 0 || args.length > 1) {
            usage();
        }

        // registerDirectory directory and process its events
        Path dir = Paths.get(args[0]);
        new Monitor(dir, OrderingRule.VOLUME).processEvents();
    }
}
