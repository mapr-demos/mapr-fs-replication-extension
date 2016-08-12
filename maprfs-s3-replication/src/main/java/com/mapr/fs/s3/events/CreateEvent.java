package com.mapr.fs.s3.events;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.mapr.fs.Config;
import com.mapr.fs.dao.ConsumerDAO;
import com.mapr.fs.events.Event;
import com.mapr.fs.messages.Create;
import com.mapr.fs.messages.Message;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateEvent extends S3Event {


  public CreateEvent(Message message, String bucket, String accessKey, String secretKey) {
    super(message, bucket, accessKey, secretKey);
  }

  @Override
  public void execute(String volumePath) throws IOException {
    Create createMessage = (Create)message;
    String filePath = volumePath + "/" + createMessage.getName();
    Path path = Paths.get(filePath);

    log.info("Executing create event: " + filePath);
    File file = new File(filePath);
    if (!createMessage.isDirectory()) {

      log.info("send file to S3");
      this.sendFile(bucket, path, createMessage.getName());


      log.info("File created: " + filePath);
    } else {
      log.info("Create Folder");
      createFolder(bucket, createMessage.getName());
    }
    log.info( filePath );
  }

  @Override
  public String getFileName() {
    return ((Create)message).getName();
  }

  @Override
  public String getFileStatus() {
    return message.getClass().getSimpleName();
  }

}
