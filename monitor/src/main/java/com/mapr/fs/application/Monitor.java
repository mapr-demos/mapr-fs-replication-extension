package com.mapr.fs.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.mapr.fs.*;
import com.mapr.fs.dao.MonitorDAO;

import com.mapr.fs.messages.Create;
import com.mapr.fs.messages.Delete;
import com.mapr.fs.messages.RenameTo;
import com.mapr.fs.messages.RenameFrom;
import com.mapr.fs.messages.Modify;

import com.mapr.fs.dao.VolumeDAO;
import com.mapr.fs.dao.dto.MonitorDTO;
import org.apache.commons.cli.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.IOException;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.nio.file.WatchKey;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.FileSystems;
import java.nio.file.WatchEvent;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import java.util.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

/**
 * Watches a directory for changes and writes those changes as appropriate
 * to a MapR stream.
 */
public class Monitor {

    private static final Logger log = Logger.getLogger(Monitor.class);
    private final int MAX_MODIFY_SIZE = 5;
    private final int MAX_OFFSETS_SIZE = 20_000;

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private Path root;
    private OrderingRule order;
    private Queue<FileOperation> changeBuffer;
    private Map<Object, FileOperation> changeMap;
    private final String volumeName;
    private Set volumes;

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
        log.info("Watching initiated");
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                keys.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });
        log.info("File tree built");
    }

    private MonitorDAO monitorDao = new MonitorDAO();
    private Map<Path, Object> inodes = Maps.newHashMap();

    /**
     * Creates a WatchService and registers the given directory
     */
    public Monitor(String volumeName, Path dir, OrderingRule order, Set volumes) throws IOException {
        this.volumeName = volumeName;
        log.info(volumeName);
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        changeBuffer = new ConcurrentLinkedQueue<>();
        changeMap = new HashMap<>();
        this.volumes = volumes;

        this.root = dir;
        this.order = order;
        watch(this.root);
    }

    /**
     * Process all events for keys queued to the watcher
     */
    private void processEvents() throws IOException {
        changeBuffer = new LinkedList<>();
        changeMap = new HashMap<>();

        startBufferProcessor();

        while (true) {
            if (!volumes.contains(volumeName)) {
                break;
            }
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                log.error("WatchKey not recognized!!");
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

    //TODO why is buffer process in another thread? That makes testing much harder.
    private void startBufferProcessor() {
        new Thread(() -> {
            JsonProducer producer = new JsonProducer("kafka.producer.", "kafka.common.");

            while (true) {
                try {
                    processBufferedEvents(producer);
                    // TODO how is this sleep justified? Shouldn't we wake up as soon as the next timeout will expire?
                    Thread.sleep(1000);
                } catch (IOException | InterruptedException e) {
                    log.error(e);
                }
            }
        }).start();
    }

    /**
     * Adds a single event to the correct queues, merging to an existing event if appropriate.
     * <p>
     * Exposed for testing only.
     *
     * @param watchDir Directory being watched
     * @param event    The event to merge
     * @throws IOException If we can't stat the file to get a unique key
     */
    public void bufferEvent(Path watchDir, WatchEvent<Path> event) throws IOException {
        if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
            processDeleteEvent(watchDir, event);
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
            processCreateEvent(watchDir, event);
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
            processModifyEvent(watchDir, event);
        }
    }

    private void processDeleteEvent(Path watchDir, WatchEvent<Path> event) {
        Path filePath = watchDir.resolve(event.context());
        log.info(ENTRY_DELETE + ": " + filePath);

        // TODO: is this correct? I added it during a merge with other code.
        // buffer in case of a rename
        FileOperation op = FileOperation.delete(watchDir, event);
        changeBuffer.add(op);

        // also record pointer back to this op so that a later create can be added
        Object key = inodes.get(filePath);
        if (key != null) {
            changeMap.put(key, op);
        } // TODO should this have an else to record an unexpected event?
    }

    private void processCreateEvent(Path watchDir, WatchEvent<Path> event) throws IOException {
        // check buffer in case this is the second half of a rename
        Path filePath = watchDir.resolve(event.context());
        log.info(ENTRY_CREATE + ": " + filePath);

        Object k = FileState.fileKey(filePath);
        inodes.put(filePath, k);

        FileOperation op = changeMap.get(k);
        if (op != null) {
            // this is the second part of the rename
            // we are adding the new directory and file name to the operation
            op.addCreate(watchDir, event);
            inodes.remove(op.getDeletePath());
            FileState fs = monitorDao.remove(op.getDeletePath());
            changeMap.remove(k);
            if (fs != null) {
                monitorDao.put(fs.toJSON());
            }
            if (op.isOldCreate()) {
                changeBuffer.add(FileOperation.create(watchDir, event));
            }
        } else {
            // this is a stand-alone creation
            changeBuffer.add(FileOperation.create(watchDir, event));
        }
    }

    private void processModifyEvent(Path watchDir, WatchEvent<Path> event) throws IOException {
        Path filePath = watchDir.resolve(event.context());
        log.info(ENTRY_MODIFY + ": " + filePath);

        FileState oldState = monitorDao.get(filePath);
        FileState newState = FileState.getFileInfo(filePath);
        changeBuffer.add(FileOperation.modify(watchDir, event, newState.changedBlockOffsets(oldState)));
        monitorDao.put(newState.toJSON());
    }

    /**
     * Processes events in the order they were buffered, but only if they are ready.
     * The only things that can be not ready are deletes or creates that are waiting
     * to be promoted into rename events.
     * <p>
     * Exposed for testing only.
     *
     * @param producer Where to send the events
     * @throws IOException If we can't read the file contents to find changes or we can't
     *                     send the message.
     */
    public void processBufferedEvents(JsonProducer producer) throws IOException {
        // process any events that we can process
        while (changeBuffer.size() > 0) {
            FileOperation op = changeBuffer.peek();
            if (op.isRename()) {
                emitRename(producer, op.getDeletePath(), op.getCreatePath());
                // TODO need to check here for changes just before the rename that might have been missed
            } else if (op.isOldDelete()) {
                log.info(String.format("Op %s is %.3f s old\n", op, System.nanoTime() / 1e9 - op.start));
                emitDelete(producer, op.getDeletePath());
            } else if (op.isOldCreate()) {
                log.info(String.format("Op %s is %.3f s old\n", op, System.nanoTime() / 1e9 - op.start));
                emitCreate(producer, op.getCreatePath());
            } else if (op.isModify()) {
                // handling changes is a bit tricky because the file may have been deleted
                // by the time we come a' knocking
                Path changed = op.getModifyPath();
                FileState newState = FileState.getFileInfo(changed);
                if (newState != null) {
                    for (List<Long> offsets : Lists.partition(op.getModifiedOffsets(), MAX_OFFSETS_SIZE)) {
                        log.info(String.format("Op %s is %.3f s old\n", op, System.nanoTime() / 1e9 - op.start));
                        emitModify(producer, changed, newState.getFileSize(), offsets,
                                newState.changedBlockContentEncoded(offsets));
                        monitorDao.put(newState.toJSON());
                    }
                } else {
                    // if file was deleted before we saw the change,
                    // we just forget about it and any changes that might have happened
                    // just before it disappeared. We shouldn't emit the delete event here
                    // since it will be coming shortly
                    monitorDao.remove(changed);
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
        List<Long> fileStateResult = new LinkedList<>();
        List<String> changesResult = new LinkedList<>();

        Iterator<List<Long>> fileStateIterator = Lists.partition(fileState, MAX_MODIFY_SIZE).iterator();
        Iterator<List<String>> changesIterator = Lists.partition(changes, MAX_MODIFY_SIZE).iterator();

        for (; fileStateIterator.hasNext() && changesIterator.hasNext(); ) {
            fileStateResult.addAll(fileStateIterator.next());
            changesResult.addAll(changesIterator.next());

            producer.send(Config.getMonitorTopic(volumeName), order.messageKey(root, name),
                    new Modify(root.relativize(name), size, fileStateResult, changesResult));

            fileStateResult.clear();
            changesResult.clear();
            log.info("send to stream -> MODIFY");
        }
    }

    private void emitCreate(JsonProducer producer, Path name) throws JsonProcessingException {
        producer.send(Config.getMonitorTopic(volumeName), order.messageKey(root, name),
                new Create(root.relativize(name), name.toFile().isDirectory()));
        log.info("send to stream -> CREATE");
    }

    private void emitDelete(JsonProducer producer, Path name) throws JsonProcessingException {
        producer.send(Config.getMonitorTopic(volumeName), order.messageKey(root, name),
                new Delete(root.relativize(name)));
        log.info("send to stream -> DELETE");
    }

    private void emitRename(JsonProducer producer, Path oldName, Path newName) throws JsonProcessingException {
        producer.send(Config.getMonitorTopic(volumeName), order.messageKey(root, oldName),
                new RenameFrom(root.relativize(oldName), root.relativize(newName)));
        producer.send(Config.getMonitorTopic(volumeName), order.messageKey(root, newName),
                new RenameTo(root.relativize(oldName), root.relativize(newName)));
        log.info("send to stream -> RENAME");
    }

    /**
     * Exposed for testing purposes.
     *
     * @return A reference to the internal change buffer.
     */
    public Queue<FileOperation> getChangeBuffer() {
        return changeBuffer;
    }

    public void recordFileState(Path f) throws IOException {
        monitorDao.put(FileState.getFileInfo(f).toJSON());
    }

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {

        Util.setConfigPath(args);

        try {
            Set<String> volumes = Collections.synchronizedSet(new HashSet<String>());
            VolumeDAO dao = new VolumeDAO();

            BasicConfigurator.configure();
            startMonitoring(volumes, dao);
        } catch (Exception ex) {
            log.info(ex);
        }
    }

    private static void startMonitoring(Set<String> volumes, VolumeDAO dao) throws IOException, InterruptedException {
        ExecutorService service = Executors.newCachedThreadPool();

        while (true) {
            for (MonitorDTO dto : dao.getAllVolumes()) {
                if (dto.isMonitoring()) {
                    if (!volumes.contains(dto.getName())) {
                        Path path = Paths.get(dto.get_id());
                        service.submit(() -> {
                            try {
                                new Monitor(dto.getName(), path, OrderingRule.VOLUME, volumes).processEvents();
                            } catch (IOException e) {
                                log.error("Cannot create Gateway" + e.getMessage());
                            }
                        });
                        volumes.add(dto.getName());
                    }
                } else {
                    if (volumes.contains(dto.getName())) {
                        volumes.remove(dto.getName());
                    }
                }
            }
            Thread.sleep(1000);
        }
    }
}
