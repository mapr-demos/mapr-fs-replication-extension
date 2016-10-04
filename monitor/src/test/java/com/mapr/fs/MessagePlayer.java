package com.mapr.fs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.mapr.fs.application.Monitor;
import com.mapr.fs.events.FileOperationNotification;
import com.mapr.fs.events.RealEvent;
import com.mapr.fs.events.SimEvent;
import com.mapr.fs.messages.Message;
import com.mapr.fs.utils.MapperUtil;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

/**
 * Replays messages read from a resource file.
 */
public class MessagePlayer {
    public static void runScript(String inputFile, String rootDir, Monitor monitor) throws IOException, InterruptedException {
        Queue<FileOperation> changes = monitor.getChangeBuffer();
        Class<SimEvent> simEventClass = SimEvent.class;
        replay(inputFile, (JsonNode in) -> MessagePlayer.expand(in, simEventClass, rootDir),
                event -> handleEvent(event, changes));
    }

    public static void handleEvent(SimEvent event, Queue<FileOperation> eventBuffer) throws IOException {
        if (event instanceof RealEvent) {
            ((RealEvent) event).doit();
        } else {
            eventBuffer.add(((FileOperationNotification) event).action);
        }
    }

    public static <T> void replay(String inputFile, Function<JsonNode, T> translator, MessageHandler<T> handler)
            throws IOException, InterruptedException {
        JsonNode messages = new ObjectMapper().readTree(Resources.getResource(inputFile));
        double offset = 0;
        for (JsonNode message : messages) {
            // none of the Message types actually has a time field, but that doesn't hurt
            JsonNode time = message.get("time");
            if (time != null) {
                double t = time.asDouble();
                double now = System.nanoTime() / 1e9 - offset;
                if (offset == 0) {
                    // this will happen only the first time around the maypole
                    offset = now;
                    now = 0;
                }
                if (t > now + 0.001) {
                    Thread.sleep((long) ((t - now) * 1e3));
                }
            }
            T translated = translator.apply(message);
            handler.handle(translated);
        }
    }

    public static List<Message> replay(String in) throws IOException, InterruptedException {
        List<Message> r = Lists.newArrayList();
        replay(in, Message::fromJson, r::add);
        return r;
    }

    public static void randomGoo(Path f, int length) throws IOException {
        Random gen = new Random(f.toString().hashCode() + length * 7907);
        byte[] r = new byte[length];
        gen.nextBytes(r);
        try (FileOutputStream out = new FileOutputStream(f.toFile())) {
            out.write(r);
        }
    }

    public static JsonNode expandStrings(ObjectMapper mapper, JsonNode in, Object... var) {
        if (in.isTextual()) {
            return new TextNode(MessageFormat.format(in.asText(), var));
        } else if (in.isArray()) {
            ArrayNode r = new ArrayNode(mapper.getNodeFactory());
            for (JsonNode child : in) {
                r.add(expandStrings(mapper, child, var));
            }
            return r;
        } else if (in.isObject()) {
            ObjectNode r = new ObjectNode(mapper.getNodeFactory());
            Iterator<Map.Entry<String, JsonNode>> ix = in.fields();
            while (ix.hasNext()) {
                Map.Entry<String, JsonNode> child = ix.next();
                r.set(child.getKey(), expandStrings(mapper, child.getValue(), var));
            }
            return r;
        } else {
            return in;
        }
    }

    public static <T> T expand(JsonNode in, Class<T> cl, String... var) {
        ObjectMapper mapper = MapperUtil.getObjectMapper();
        return mapper.convertValue(expandStrings(mapper, in, var), cl);
    }

    public static void verifyMessages(List<ProducerRecord<String, String>> history, String reference) throws IOException, InterruptedException {
        List<Message> actual = Lists.transform(history, record -> Message.fromJson(record.value()));
        List<Message> ref = Lists.newArrayList();
        replay(reference, Message::fromJson, ref::add);
        assertEquals(ref, actual);
    }

    public static interface MessageHandler<T> {
        void handle(T m) throws IOException;
    }

    public static interface Translator<T> {
        T translate(JsonNode json);
    }
}
