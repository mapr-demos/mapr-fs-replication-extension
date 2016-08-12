package com.mapr.fs.s3.events;

import com.mapr.fs.messages.Delete;
import com.mapr.fs.messages.Message;

import java.io.IOException;


public class DeleteEvent extends S3Event {

  public DeleteEvent(Message message, String bucket, String accessKey, String secretKey) {
    super(message, bucket, accessKey, secretKey);
  }

  @Override
  public void execute(String volumePath) throws IOException {
    log.info("Delete object in S3");
    this.deleteObject(bucket, ((Delete)message).getName() );

  }

  public String getFileName() {
    return ((Delete)message).getName();
  }

  public String getFileStatus() {
    return message.getClass().getSimpleName();
  }

}
