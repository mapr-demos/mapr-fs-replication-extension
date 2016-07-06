package com.mapr.fs;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Consumer {
    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();

        for (String val : args) {
            String[] arr = val.split(":");

            if (!map.containsKey(arr[0]))
                map.put(arr[0], arr[1]);
            else {
                throw new RuntimeException("Try to add existed volume");
            }
        }

        for (Map.Entry<String, String> pair : map.entrySet()) {
            new Thread(() -> {
                new Gateway(pair.getKey(), pair.getValue()).processEvents();
            }).start();
        }
    }

    public static class Gateway {

        private final String topic;
        private final String volumeName;
        private final String path;

        public Gateway(String volumeName, String path) {
            this.volumeName = volumeName;
            this.topic = String.format(Config.MONITOR_TOPIC, volumeName);
            this.path = path;

            System.out.println(volumeName + " gateway configured with path " + path);
        }

        private void processEvents() {
            System.out.println(volumeName + " gateway started");
            KafkaConsumer<String, String> consumer = null;
            try {

                Config config = new Config("kafka.consumer.", "kafka.common.");

                consumer = new KafkaConsumer<>(config.getProperties());
                consumer.subscribe(Arrays.asList(config.getTopicName(topic)));
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(200);
                    for (ConsumerRecord<String, String> record : records) {
                        System.out.println(volumeName + ": " + record);
                    }
                    consumer.commitSync();
                }
            } finally {
                if (consumer != null) {
                    consumer.close();
                }
            }
        }
    }
}
