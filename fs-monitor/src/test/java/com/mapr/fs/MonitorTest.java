package com.mapr.fs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.mapr.fs.application.Monitor;
import com.mapr.fs.config.Config;
import com.mapr.fs.events.SimEvent;
import junit.framework.TestCase;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Queue;

public class MonitorTest extends TestCase {
    public void testBasicFileOps() throws IOException, InterruptedException {
        File dir = Files.createTempDir();
        Monitor mon = new Monitor("test", dir.toPath(), Monitor.OrderingRule.VOLUME);
        MessagePlayer.runScript("events-1.json", dir.toString(), mon);
        JsonProducer producer = getMockProducer();
        mon.processBufferedEvents(producer);
        List<ProducerRecord<String, String>> history = ((MockProducer<String, String>) producer.getActualProducer()).history();
        MessagePlayer.verifyMessages(history, "messages-1.json");
    }

    public void testReplay() throws IOException, InterruptedException {
        List<SimEvent> messages = Lists.newArrayList();
        MessagePlayer.replay("events-2.json", in-> MessagePlayer.expand(in, SimEvent.class, "root"), messages::add);
        System.out.printf("%s\n", messages);
    }

    public void testNoEventsTooEarly() throws IOException, InterruptedException {
        File dir = Files.createTempDir();
        try {
            Monitor mon = new Monitor("test", dir.toPath(), Monitor.OrderingRule.VOLUME);
            // make timeout really long to make debugging safer
            FileOperation.setMaxTimeForRename(60);

            Path f = new File(dir, "f1").toPath();
            MessagePlayer.randomGoo(f, 10200);
            mon.recordFileState(f);

            JsonProducer producer = getMockProducer();

            ObjectMapper mapper = new ObjectMapper();
            Queue<FileOperation> buf = mon.getChangeBuffer();
            MessagePlayer.replay("events-1.json", json -> {
                FileOperation x = mapper.convertValue(json, FileOperation.class);
                x.setRoot(dir);
                return x;
            }, buf::add);
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
            buf.add(FileOperation.create(dir.toPath(), WatchEventImpl.create(f)));
            new FileOutputStream(f.toFile()).close();
            FileState fs = FileState.getFileInfo(f);

            // modify it
            try (PrintWriter fout = new PrintWriter(f.toFile())) {
                fout.printf("testing\n");
            }
            FileState newFs = FileState.getFileInfo(f);
            buf.add(FileOperation.modify(dir.toPath(), WatchEventImpl.modify(f), newFs.changedBlockOffsets(fs)));
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
            buf.add(FileOperation.modify(dir.toPath(), WatchEventImpl.modify(f), newFs.changedBlockOffsets(fs)));

            // all three events should slide through now
            mon.processBufferedEvents(producer);

            history = ((MockProducer<String, String>) producer.getActualProducer()).history();
            assertEquals(3, history.size());

            ObjectMapper mapper = Util.getObjectMapper();
            String[] types = {"create", "change", "change"};
            for (int i = 0; i < 3; i++) {
                assertEquals(Config.getMonitorTopic("/fsmonitor:change_test"), history.get(i).topic());
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
            mon.bufferEvent(f1, WatchEventImpl.create(f1));

            // modify it
            try (PrintWriter fout = new PrintWriter(f1.toFile())) {
                fout.printf("testing\n");
            }
            mon.bufferEvent(f1, WatchEventImpl.modify(f1));

            // rename it, meaning delete and create
            Path f2 = new File(dir, "f2").toPath();
            f1.toFile().renameTo(f2.toFile());
            mon.bufferEvent(dir.toPath(), WatchEventImpl.delete(f1));
            mon.bufferEvent(dir.toPath(), WatchEventImpl.create(f2));

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

}