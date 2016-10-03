package com.mapr.fs.s3;

import com.mapr.fs.Config;
import com.mapr.fs.PluginConfiguration;
import com.mapr.fs.events.Event;
import com.mapr.fs.s3.events.EventFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Gateway {

    private static final Logger log = Logger.getLogger(Gateway.class);

    private final PluginConfiguration pluginConfiguration;
    private final String topic;
    private final String volumeName;
    private final EventFactory factory;
    private ConcurrentHashMap running;


    public Gateway(String volumeName, ConcurrentHashMap running, PluginConfiguration pluginConfiguration) throws IOException {
        this.pluginConfiguration = pluginConfiguration;
        this.volumeName = volumeName;
        this.running = running;
        this.topic = Config.getMonitorTopic(volumeName);
        this.factory = new EventFactory(pluginConfiguration);
    }

    protected void processEvents() {
        log.info(volumeName + " gateway started");
        KafkaConsumer<String, String> consumer = null;
        try {
            Config config = new Config("s3.consumer.", "kafka.common.");
            Properties properties = config.getProperties();


            consumer = new KafkaConsumer<>(properties);
            log.info("Subscribing to the topic " + config.getTopicName(topic));
            consumer.subscribe(Collections.singletonList(config.getTopicName(topic)));
            while (true) {
                if (!running.containsKey(pluginConfiguration.getBucketName() + volumeName)) {
                    log.info("In " + pluginConfiguration.getBucketName() + " bucket stopped consuming through => " + volumeName);
                    break;
                }
                ConsumerRecords<String, String> records = consumer.poll(200);
                for (ConsumerRecord<String, String> record : records) {
                    log.info(String.format("volumeName : %s (topic = %s, partition = %s, offset = %s, timestamp = %s, producer = %s, key = %s)",
                            volumeName,
                            record.topic(),
                            record.partition(),
                            record.offset(),
                            record.timestamp(),
                            record.producer(),
                            record.key())
                    );
                    Event event = factory.parseEvent(record.value());
                    if (event != null) {
                        event.execute(record.key());
                    }
                }
                consumer.commitSync();
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
    }

}

