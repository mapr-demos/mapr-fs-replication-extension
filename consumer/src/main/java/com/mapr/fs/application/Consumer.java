package com.mapr.fs.application;

import com.mapr.fs.*;
import com.mapr.fs.dao.ClusterDAO;
import com.mapr.fs.dao.VolumeStatusDao;
import com.mapr.fs.dao.dto.FileStatusDto;
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
import java.time.LocalDateTime;
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

        private VolumeStatusDao volumeStatusDao;

        public Gateway(String volumeName, String path, Set volumes) throws IOException {
            this.volumeName = volumeName;
            this.volumes = volumes;
            this.topic = Config.getMonitorTopic(volumeName);
            this.path = path;
            this.factory = new EventFactory();
            this.volumeStatusDao = new VolumeStatusDao();
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
                        Event event = factory.parseEvent(record.value());
                        // log metada, to not log the full message ,( esp. value that can contains file content)
                        log.info(String.format("volumeName : %s (topic = %s, partition = %s, offset = %s, timestamp = %s, producer = %s, key = %s)",
                                volumeName,
                                record.topic(),
                                record.partition(),
                                record.offset(),
                                record.timestamp(),
                                record.producer(),
                                record.key())
                        );
                        volumeStatusDao.putFileStatusByVolumeName(volumeName, new FileStatusDto(event.getFileName(), event.getFileStatus(), LocalDateTime.now().toString()));
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

        Util.setConfigPath(args);

        Set<String> volumes = Collections.synchronizedSet(new HashSet<String>());
        ClusterDAO dao = new ClusterDAO();
        BasicConfigurator.configure();

        startConsuming(volumes, dao);
    }

    private static void startConsuming(Set<String> volumes, ClusterDAO dao) throws IOException, InterruptedException {
        ExecutorService service = Executors.newCachedThreadPool();

        while (true) {
            for (VolumeDTO dto : dao.getAllVolumes()) {
                if (dto.isReplicating()) {
                    if (!volumes.contains(dto.getName())) {
                        String replicationFolderForVolume = checkDir(dto.getPath(), dto.getName());
                        log.info("Replication files/events will be saved in "+ replicationFolderForVolume + "/" + dto.getName());
                        service.submit(() -> {
                            try {
                                new Gateway(dto.getName(), replicationFolderForVolume, volumes).processEvents();
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

    /**
     * Create the target folder if it does not exist
     *
     * @param replicationTargetFolder parent folder for "all replication" events for this consumer instance
     * @param topicName               Topic name, should be the name of the source volume
     */
    protected static String checkDir(String replicationTargetFolder, String topicName) {
        File file = new File(replicationTargetFolder, topicName);
        if (!file.exists()) {
            file.mkdirs();
            log.info(file + " created");
        }
        return file.getAbsolutePath();
    }
}
