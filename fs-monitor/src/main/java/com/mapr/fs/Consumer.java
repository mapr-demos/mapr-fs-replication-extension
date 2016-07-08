package com.mapr.fs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.fs.events.Event;
import com.mapr.fs.events.EventFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Consumer {

    private static final Logger log = Logger.getLogger(Consumer.class);

    public static class Gateway {

        private final String topic;
        private final String volumeName;
        private final String path;
        private final EventFactory factory;

        public Gateway(String volumeName, String path) {
            this.volumeName = volumeName;
            this.topic = Config.getMonitorTopic(volumeName);
            this.path = path;
            factory = new EventFactory();
            log.info(volumeName + " gateway configured with path " + path);
        }

        private void processEvents() {
            log.info(volumeName + " gateway started");
            KafkaConsumer<String, String> consumer = null;
            try {

                Config config = new Config("kafka.consumer.", "kafka.common.");

                consumer = new KafkaConsumer<>(config.getProperties());
                log.info("Subscribing to the topic " + config.getTopicName(topic));
                consumer.subscribe(Arrays.asList(config.getTopicName(topic)));
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(200);
                    for (ConsumerRecord<String, String> record : records) {
                        log.info(volumeName + ": " + record);
                        Event event = factory.parseEvent(record.value());
                        event.execute(path);
                    }
                    consumer.commitSync();
                }
            } catch (IOException e) {
                log.error(e);
            } finally {
                if (consumer != null) {
                    consumer.close();
                }
            }
        }
    }


    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();

        BasicConfigurator.configure();

        for (String val : args) {
            String[] arr = val.split(":");

            if (!map.containsKey(arr[0]))
                map.put(arr[0], arr[1]);
            else {
                log.warn("Trying to add existed volume");
//                    throw new IllegalArgumentException("Trying to add existed volume");
            }
        }

        ExecutorService service = Executors.newFixedThreadPool(map.size());

        for (Map.Entry<String, String> pair : map.entrySet()) {

            service.submit(() ->
                new Gateway(pair.getKey(), pair.getValue()).processEvents() );

//            new Thread(() -> {
//                new Gateway(pair.getKey(), pair.getValue()).processEvents();
//            }).start();
        }
    }
}
