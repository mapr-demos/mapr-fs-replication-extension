package com.mapr.fs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import junit.framework.TestCase;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.List;
import java.util.Queue;

public class MonitorTest extends TestCase {
    public void testProcessEvents() throws Exception {

    }

    public void testNoEventsTooEarly() throws Exception {
        File dir = Files.createTempDir();
        try {
            Monitor mon = new Monitor("test", dir.toPath(), Monitor.OrderingRule.VOLUME);
            // make timeout really long to make debugging safer
            FileOperation.setMaxTimeForRename(60);

            Path f = new File(dir, "f1").toPath();
            new FileOutputStream(f.toFile()).close();
            mon.recordFileState(f);

            JsonProducer producer = getMockProducer();

            Queue<FileOperation> buf = mon.getChangeBuffer();
            buf.add(FileOperation.modify(dir.toPath(), Event.modify(f), ImmutableList.of(0L, 8192L)));
            buf.add(FileOperation.delete(dir.toPath(), Event.delete(f)));
            buf.add(FileOperation.modify(dir.toPath(), Event.modify(f), ImmutableList.of(2 * 8192L)));
            mon.processBufferedEvents(producer);

            // the first modify record should come through, but the
            // subsequent delete should hang and block the following modify as well
            List<ProducerRecord<String, String>> history = ((MockProducer<String, String>) producer.getActualProducer()).history();
            assertEquals(1, history.size());
            JsonNode v = Util.getObjectMapper().readTree(history.get(0).value());
            assertEquals("change", v.get("type").asText());
            assertEquals(f.toString(), v.get("name").asText());
        } finally {
            dir.delete();
        }
    }

    public void testDelayAllowsEvents() throws Exception {
        File dir = Files.createTempDir();
        try {
            Monitor mon = new Monitor("test", dir.toPath(), Monitor.OrderingRule.VOLUME);
            FileOperation.setMaxTimeForRename(2);

            Path f = new File(dir, "f1").toPath();

            JsonProducer producer = getMockProducer();

            // create an empty file
            Queue<FileOperation> buf = mon.getChangeBuffer();
            buf.add(FileOperation.create(dir.toPath(), Event.create(f)));
            new FileOutputStream(f.toFile()).close();
            FileState fs = FileState.getFileInfo(f);

            // modify it
            try (PrintWriter fout = new PrintWriter(f.toFile())) {
                fout.printf("testing\n");
            }
            FileState newFs = FileState.getFileInfo(f);
            buf.add(FileOperation.modify(dir.toPath(), Event.modify(f), newFs.changedBlockOffsets(fs)));
            fs = newFs;

            mon.processBufferedEvents(producer);

            // the create should hang the events up
            List<ProducerRecord<String, String>> history = ((MockProducer<String, String>) producer.getActualProducer()).history();
            assertEquals(0, history.size());

            // sleeping for a bit should unclog things
            Thread.sleep((long) (FileOperation.maxTimeForRename * 1000 * 2));

            // modify the file again
            try (PrintWriter fout = new PrintWriter(f.toFile())) {
                fout.printf("testing anew\n");
            }
            newFs = FileState.getFileInfo(f);
            buf.add(FileOperation.modify(dir.toPath(), Event.modify(f), newFs.changedBlockOffsets(fs)));

            // all three events should slide through now
            mon.processBufferedEvents(producer);

            history = ((MockProducer<String, String>) producer.getActualProducer()).history();
            assertEquals(3, history.size());

            ObjectMapper mapper = Util.getObjectMapper();
            String[] types = {"create", "change", "change"};
            for (int i = 0; i < 3; i++) {
                assertEquals(Config.getMonitorTopic("volume"), history.get(i).topic());
                assertEquals(dir.getAbsolutePath(), history.get(i).key());
                JsonNode c = mapper.readTree(history.get(i).value());
                assertEquals(f.toString(), c.get("name").asText());
                assertEquals(types[i], c.get("type").asText());
            }
        } finally {
            dir.delete();
        }
    }

    public void testRename() throws Exception {
        File dir = Files.createTempDir();
        try {
            Monitor mon = new Monitor("test", dir.toPath(), Monitor.OrderingRule.FILE);
            FileOperation.setMaxTimeForRename(2000);

            Path f1 = new File(dir, "f1").toPath();

            // create an empty file
            new FileOutputStream(f1.toFile()).close();
            mon.bufferEvent(f1, Event.create(f1));

            // modify it
            try (PrintWriter fout = new PrintWriter(f1.toFile())) {
                fout.printf("testing\n");
            }
            mon.bufferEvent(f1, Event.modify(f1));

            // rename it, meaning delete and create
            Path f2 = new File(dir, "f2").toPath();
            f1.toFile().renameTo(f2.toFile());
            mon.bufferEvent(dir.toPath(), Event.delete(f1));
            mon.bufferEvent(dir.toPath(), Event.create(f2));

            FileOperation.setMaxTimeForRename(1);
            Thread.sleep((long) (FileOperation.maxTimeForRename * 1000 * 2));

            // all the events should slide through now. There should be four
            // events because the delete/create pair should be fused into a
            // single rename which, in turn, gets emitted as a
            // rename_from/rename_to pair
            JsonProducer producer = getMockProducer();
            mon.processBufferedEvents(producer);

            List<ProducerRecord<String, String>> history = ((MockProducer<String, String>) producer.getActualProducer()).history();
            assertEquals(4, history.size());

            ObjectMapper mapper = Util.getObjectMapper();
            String[] types = {"create", "change", "rename_from", "rename_to"};
            String[] keys = {f1.toString(), f1.toString(), f1.toString(), f2.toString()};
            for (int i = 0; i < 3; i++) {
                assertEquals(Config.getMonitorTopic("volume"), history.get(i).topic());
                assertEquals(keys[i], history.get(i).key());
                JsonNode c = mapper.readTree(history.get(i).value());
                assertEquals(f1.toString(), c.get("name").asText());
                assertEquals(types[i], c.get("type").asText());
            }
        } finally {
            dir.delete();
        }
    }

    public JsonProducer getMockProducer() {
        String confPath = Thread.currentThread().getContextClassLoader().getResource("test_config.conf").getPath();
        Config config = new Config(confPath, new String[]{"kafka.producer.", "kafka.common."});
        return new JsonProducer(new MockProducer<>(true, new StringSerializer(), new StringSerializer()), config);
    }

    private static class Event<T> implements WatchEvent<T> {
        private final Kind<T> kind;
        private final T context;
        private int count;

        Event(Kind<T> kind, T context) {
            this.kind = kind;
            this.context = context;
            this.count = 1;
        }

        public Kind<T> kind() {
            return this.kind;
        }

        public T context() {
            return this.context;
        }

        public int count() {
            return this.count;
        }

        void increment() {
            ++this.count;
        }

        static Event<Path> delete(Path f) {
            return new Event<>(StandardWatchEventKinds.ENTRY_DELETE, f);
        }

        static Event<Path> create(Path f) {
            return new Event<>(StandardWatchEventKinds.ENTRY_CREATE, f);
        }

        static Event<Path> modify(Path f) {
            return new Event<>(StandardWatchEventKinds.ENTRY_MODIFY, f);
        }
    }
}