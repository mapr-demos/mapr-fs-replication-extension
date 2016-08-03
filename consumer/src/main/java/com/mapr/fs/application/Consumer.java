package com.mapr.fs.application;

import com.mapr.fs.Config;
import com.mapr.fs.dao.ClusterDAO;
import com.mapr.fs.dao.dto.VolumeDTO;
import com.mapr.fs.events.Event;
import com.mapr.fs.events.EventFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


public class Consumer {

    private static final Logger log = Logger.getLogger(Consumer.class);

    public static class Gateway {

        private final String topic;
        private final String volumeName;
        private final String path;
        private final EventFactory factory;
        private Set volumes;

        public Gateway(String volumeName, String path, Set volumes) {
            this.volumeName = volumeName;
            this.volumes = volumes;
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
                    if (!volumes.contains(volumeName)) {
                        break;
                    }
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

    public static void main(String[] args) throws Exception {

        Set<String> volumes = new CopyOnWriteArraySet<>();
        ClusterDAO dao = new ClusterDAO();
        BasicConfigurator.configure();

        Config conf = new Config("cluster.");
        String replicationTargetFolder= conf.getProperties().getProperty("target_folder");

        if ( replicationTargetFolder == null || replicationTargetFolder.isEmpty()) {
            log.error("Configuration should contain  the 'cluster.target_folder' proprety ");
            System.exit(1);
        }

        // Adding a trailing slash if not present (since it used to save replicated files)
        if(replicationTargetFolder.charAt(replicationTargetFolder.length()-1)!=File.separatorChar){
            replicationTargetFolder += File.separator;
        }

        log.info("Replication files/events will be saved in "+ replicationTargetFolder);

        startConsuming(volumes, dao, replicationTargetFolder);
    }

    private static void startConsuming(Set<String> volumes, ClusterDAO dao, String CLUSTER_NAME) throws IOException, InterruptedException {
        ExecutorService service = Executors.newWorkStealingPool();

        while (true) {
            for (VolumeDTO dto : dao.getAllVolumes()) {
                if (dto.isReplicating()) {
                    if (!volumes.contains(dto.getName())) {
                        checkDir(CLUSTER_NAME, dto);
                        service.submit(() ->
                            new Gateway(dto.getName(), CLUSTER_NAME + dto.getName(), volumes).processEvents());
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

    private static void checkDir(String CLUSTER_NAME, VolumeDTO dto) {
        File file  = new File(CLUSTER_NAME + dto.getName());
        if (!file.exists())
            file.mkdir();
    }
}
