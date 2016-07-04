package com.mapr.fs;

import com.google.common.collect.Maps;
import com.mapr.fs.messages.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Watches a directory for changes and writes those changes as appropriate
 * to a MapR stream.
 */
public class Monitor {
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private Path root;
    private OrderingRule order;

    enum OrderingRule {
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

    /**
     * Creates a WatchService and registers the given directory
     */
    Monitor(Path dir, OrderingRule order) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();

        watch(dir, order);
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() throws IOException {
        Queue<FileOperation> changeBuffer = new LinkedList<>();
        Map<Object, FileOperation> changeMap = new HashMap<>();
        KafkaProducer<String, String> producer = new KafkaProducer<>(
                Config.getConfig().getPrefixedProps("kafka.producer.", "kafka.common.")
        );

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

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                @SuppressWarnings("unchecked") WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path name = ev.context();
                Path child = dir.resolve(name);

                System.out.println(event.kind());
                if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    // buffer in case of a rename
                    FileOperation op = FileOperation.delete(ev);
                    changeBuffer.add(op);
                    changeMap.put(FileState.fileKey(ev.context()), op);
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    // check buffer in case this is the second half of a rename
                    Object k = FileState.fileKey(ev.context());
                    FileOperation op = changeMap.get(k);
                    System.out.println(op);
                    if (op != null) {
                        op.addCreate(ev);
                    } else {
                        changeBuffer.add(FileOperation.create(ev));
                    }
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    // emit any buffered changes or any deletes that are old enough from the front of the buffer
                    // if buffered items remain, buffer this one as well
                    // if the buffer has been emptied check to find out which blocks have changed and
                    // emit those changes
                    changeBuffer.add(FileOperation.modify(ev));
                }
                System.out.println(changeBuffer.size());

                // process any events that we can process
                while (changeBuffer.size() > 0) {
                    FileOperation op = changeBuffer.peek();
                    if (op.isRename()) {
                        emitRename(producer, op.getDeletePath(), op.getCreatePath());
                    } else if (op.isOldDelete()) {
                        emitDelete(producer, op.getDeletePath());
                    } else if (op.isOldCreate()) {
                        emitCreate(producer, op.getCreatePath());
                    } else if (op.isModify()) {
                        // handling changes is a bit tricky because the file may have been deleted
                        // by the time we come a' knocking
                        Path changed = op.getModifyPath();
                        FileState newState = FileState.getFileInfo(changed);
                        if (newState != null) {
                            emitModify(producer, changed, state.get(changed).changedBlockOffsets(newState));
                            state.put(changed, newState);
                        } else {
                            // if file deleted, we just forget about it and any changes that might have happened
                            // just before it disappeared
                            state.remove(changed);
                        }
                    } else {
                        // We can only process the leading elements of the queue.
                        // This keeps things in proper order.
                        break;
                    }
                    changeBuffer.remove();
                }

                // print out event
                System.out.format("%s: %s\n", event.kind().name(), child);

                // if directory is created, and watching recursively, then
                // registerDirectory it and its sub-directories
                if ((kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                            watch(child, order);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    private void emitModify(KafkaProducer<String, String> producer, Path name, List<Long> fileState) {
        String key = order.messageKey(root, name);
        String msg = new Change(name, fileState).toString();
        producer.send(new ProducerRecord<>(Config.getConfig().getProducerTopicName("mapr.fs.monitor"), key, msg));
    }

    private void emitCreate(KafkaProducer<String, String> producer, Path name) {
        String key = order.messageKey(root, name);
        String msg = new Create(name).toString();
        producer.send(new ProducerRecord<>(Config.getConfig().getProducerTopicName("mapr.fs.monitor"), key, msg));
    }

    private void emitDelete(KafkaProducer<String, String> producer, Path name) {
        String key = order.messageKey(root, name);
        String msg = new Delete(name).toString();
        producer.send(new ProducerRecord<>(Config.getConfig().getProducerTopicName("mapr.fs.monitor"), key, msg));
    }

    private void emitRename(KafkaProducer<String, String> producer, Path oldName, Path newName) {
        String key = order.messageKey(root, oldName);
        String msg = new RenameFrom(oldName, newName).toString();
        producer.send(new ProducerRecord<>(Config.getConfig().getProducerTopicName("mapr.fs.monitor"), key, msg));

        key = order.messageKey(root, newName);
        msg = new RenameTo(oldName, newName).toString();
        producer.send(new ProducerRecord<>(Config.getConfig().getProducerTopicName("mapr.fs.monitor"), key, msg));
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
