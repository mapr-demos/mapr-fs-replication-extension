package com.mapr.fs.s3.events;

import com.mapr.fs.messages.Message;
import com.mapr.fs.messages.Modify;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class ModifyEvent extends S3Event {

    public ModifyEvent(Message message, String volumeName, String bucket, String accessKey, String secretKey) {
        super(message, bucket, volumeName, accessKey, secretKey);
    }

    @Override
    public void execute(String volumePath) throws IOException {
        Modify modifyMessage = (Modify) message;

        if (modifyMessage.isLast()) {
            String filePath = volumePath + "/" + modifyMessage.getName();
            Path path = Paths.get(filePath);
            log.info("send file to S3");
            this.sendFile(bucket, path, getFileName());
        }
    }

    public String getFileName() {
        return ((Modify) message).getName();
    }

    public String getFileStatus() {
        return message.getClass().getSimpleName();
    }


}
