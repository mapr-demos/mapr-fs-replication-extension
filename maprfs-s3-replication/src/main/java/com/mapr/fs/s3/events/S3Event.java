package com.mapr.fs.s3.events;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mapr.fs.events.Event;
import com.mapr.fs.messages.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import static com.mapr.fs.utils.AmazonS3Util.logAmazonClientExceptionInfo;
import static com.mapr.fs.utils.AmazonS3Util.logAmazonServerExceptionInfo;

@Slf4j
public abstract class S3Event implements Event {

    Message message;
    String bucket;
    String volumeName;

    private AmazonS3 s3client;

    public S3Event(Message message, String bucket, String volumeName, String accessKey, String secretKey) {
        this.message = message;
        this.bucket = bucket;
        this.volumeName = volumeName;
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        this.s3client = new AmazonS3Client(credentials);
    }

    protected void sendFile(String bucket, Path path) {
        try {
            String pathToFile = path.toString();

            log.info("Uploading a new object to S3 from a file\n");
            File file = new File(pathToFile);

            String pathToFileinS3 = getPathToFile(pathToFile, volumeName);
            log.info("Path to File in s3: " + pathToFileinS3);

            s3client.putObject(new PutObjectRequest(bucket, pathToFileinS3, file));
        } catch (AmazonServiceException ase) {
            logAmazonServerExceptionInfo(ase);
        } catch (AmazonClientException ace) {
            logAmazonClientExceptionInfo(ace);
        }
    }

    protected void createFolder(String bucket, String folder) {
        try {

            if (folder.startsWith("/")) {
                folder = folder.substring(1);
            }

            String pathToFileinS3 = getPathToFile(folder, volumeName);
            log.info("Path to File in s3: " + pathToFileinS3);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(0);
            // create empty content
            InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
            log.info("Uploading a new object to S3 from a file\n");
            // create a PutObjectRequest passing the folder name suffixed by /
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, pathToFileinS3 + "/", emptyContent, metadata);
            s3client.putObject(putObjectRequest);

        } catch (AmazonServiceException ase) {
            logAmazonServerExceptionInfo(ase);
        } catch (AmazonClientException ace) {
            logAmazonClientExceptionInfo(ace);
        }


    }


    protected void deleteObject(String bucket, String object) {
        try {

            if (object.startsWith("/")) {
                object = object.substring(1);
            }

            String pathToFileinS3 = getPathToFile(object, volumeName);
            log.info("Path to File in s3: " + pathToFileinS3);

            log.info("Delete Object " + object);
            // recursive delete
            for (S3ObjectSummary file : s3client.listObjects(bucket, pathToFileinS3).getObjectSummaries()) {
                s3client.deleteObject(bucket, file.getKey());
            }


        } catch (AmazonServiceException ase) {
            logAmazonServerExceptionInfo(ase);
        } catch (AmazonClientException ace) {
            logAmazonClientExceptionInfo(ace);
        }
    }

    private String getPathToFile(String path, String volumeName) {
        int start = path.indexOf(volumeName);
        return path.substring(start);
    }

}
