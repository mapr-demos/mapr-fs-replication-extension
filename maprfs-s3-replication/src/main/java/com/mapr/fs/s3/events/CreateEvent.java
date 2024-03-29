package com.mapr.fs.s3.events;

import com.mapr.fs.messages.Create;
import com.mapr.fs.messages.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class CreateEvent extends S3Event {


    public CreateEvent(Message message, String volumeName, String bucket, String accessKey, String secretKey) {
        super(message, bucket, volumeName, accessKey, secretKey);
    }

    @Override
    public void execute(String volumePath) throws IOException {
        Create createMessage = (Create) message;
        String filePath = volumePath + "/" + createMessage.getName();
        Path path = Paths.get(filePath);

        log.info("Executing create event: " + filePath);
        if (!createMessage.isDirectory()) {

            log.info("send file to S3");
            this.sendFile(bucket, path, getFileName());


            log.info("File created: " + filePath);
        } else {
            log.info("Create Folder");
            createFolder(bucket, getFileName());
        }
        log.info(filePath);
    }

    @Override
    public String getFileName() {
        return ((Create) message).getName();
    }

    @Override
    public String getFileStatus() {
        return message.getClass().getSimpleName();
    }

}
