package com.mapr.fs.s3.events;

import com.mapr.fs.messages.Message;
import com.mapr.fs.messages.Modify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModifyEvent extends S3Event {

  public ModifyEvent(Message message, String bucket, String accessKey, String secretKey) {
    super(message, bucket, accessKey, secretKey);
  }

  @Override
  public void execute(String volumePath) throws IOException {
    Modify modifyMessage = (Modify)message;
    String filePath = volumePath + "/" + modifyMessage.getName();
    Path path = Paths.get(filePath);
    log.info("send file to S3");
    this.sendFile(bucket, path, modifyMessage.getName());

  }

  public String getFileName() {
    return ((Modify)message).getName();
  }

  public String getFileStatus() {
    return message.getClass().getSimpleName();
  }


}
