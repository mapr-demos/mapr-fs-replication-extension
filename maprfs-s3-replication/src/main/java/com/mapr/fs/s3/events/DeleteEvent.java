package com.mapr.fs.s3.events;

import com.mapr.fs.messages.Delete;
import com.mapr.fs.messages.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class DeleteEvent extends S3Event {

    public DeleteEvent(Message message, String bucket, String accessKey, String secretKey) {
        super(message, bucket, accessKey, secretKey);
    }

    @Override
    public void execute(String volumePath) throws IOException {
        log.info("Delete object in S3");
        String filePath = volumePath + "/" + ((Delete) message).getName();
        this.deleteObject(bucket, filePath);

    }

    public String getFileName() {
        return ((Delete) message).getName();
    }

    public String getFileStatus() {
        return message.getClass().getSimpleName();
    }

}
