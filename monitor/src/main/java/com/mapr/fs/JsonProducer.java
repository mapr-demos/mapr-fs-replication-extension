package com.mapr.fs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Like a KafkaProducer, but includes magical conversion of POJO's to JSON when sending
 */
@Slf4j
public class JsonProducer {

    protected final Producer<String, String> delegate;
    private final ObjectMapper mapper = com.mapr.fs.Util.getObjectMapper();
    private Config config;

    public JsonProducer() throws IOException {
        try (InputStream props = Resources.getResource("producer.props").openStream()) {
            Properties properties = new Properties();
            properties.load(props);
            delegate = new KafkaProducer<>(properties);
        }
    }


    public JsonProducer(Producer<String, String> delegate, Config config) {
        this.delegate = delegate;
        this.config = config;
    }

    public JsonProducer(String... configPrefixes) {
        config = new Config(configPrefixes);
        delegate = new KafkaProducer<>(config.getProperties());
    }

    public void send(String topic, Object x) throws JsonProcessingException {
        send(topic, null, x);
    }

    public void send(String topic, String key, Object x) throws JsonProcessingException {
        log.info("Sending to stream: topic<" + config.getTopicName(topic) + "> key<" + key + ">");
        delegate.send(new ProducerRecord<>(config.getTopicName(topic), key, mapper.writeValueAsString(x)));
    }

    public void close() {
        delegate.close();
    }

    public void flush() {
        delegate.flush();
    }

    public Producer<String, String> getActualProducer() {
        return delegate;
    }
}
