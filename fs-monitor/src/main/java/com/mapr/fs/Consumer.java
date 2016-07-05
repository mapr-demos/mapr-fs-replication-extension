package com.mapr.fs;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;

public class Consumer {
    public static void main(String[] args) {
        KafkaConsumer<String, String> consumer = null;
        try {

            Config config = new Config("kafka.consumer.", "kafka.common.");

            consumer = new KafkaConsumer<>(config.getProperties());
            consumer.subscribe(Arrays.asList(config.getTopicName("mapr.fs.monitor")));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(200);
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println(record);
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
