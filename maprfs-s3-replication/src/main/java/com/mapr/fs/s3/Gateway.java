package com.mapr.fs.s3;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.mapr.fs.Config;
import com.mapr.fs.events.Event;
import com.mapr.fs.s3.events.EventFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

public class Gateway {

  private static final Logger log = Logger.getLogger(Gateway.class);

  private final PluginConfiguration pluginConfiguration;
  private final String topic;
  private final String volumeName;
  private final String path;
  private final EventFactory factory;
  private Set volumes;


  public Gateway(String volumeName, String path, Set volumes, PluginConfiguration pluginConfiguration) throws IOException {
    this.pluginConfiguration = pluginConfiguration;
    this.volumeName = volumeName;
    this.volumes = volumes;
    this.topic = Config.getMonitorTopic(volumeName);
    this.path = path;
    this.factory = new EventFactory(pluginConfiguration);
    log.info(volumeName + " gateway configured with path " + path);
  }

  protected void processEvents() {
    log.info(volumeName + " gateway started");
    KafkaConsumer<String, String> consumer = null;
    try {
      Config config = new Config("s3.consumer.", "kafka.common.");
      Properties properties = config.getProperties();


      consumer = new KafkaConsumer<>(properties);
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

