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
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

public abstract class S3Event implements Event {

    final Logger log = Logger.getLogger(getClass());
    Message message;
    String bucket;

    private AmazonS3 s3client;

    public S3Event(Message message, String bucket, String accessKey, String secretKey) {
        this.message = message;
        this.bucket = bucket;
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        this.s3client = new AmazonS3Client(credentials);
    }

    protected void sendFile(String bucket, Path path) {
        try {
            String pathToFile = path.toString();

            log.info("Uploading a new object to S3 from a file\n");
            File file = new File(pathToFile);

            if (pathToFile.startsWith("/")) {
                pathToFile = pathToFile.substring(1);
            }

            s3client.putObject(new PutObjectRequest(bucket, pathToFile, file));
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

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(0);
            // create empty content
            InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
            log.info("Uploading a new object to S3 from a file\n");
            // create a PutObjectRequest passing the folder name suffixed by /
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, folder + "/", emptyContent, metadata);
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

            log.info("Delete Object " + object);
            // recursive delete
            for (S3ObjectSummary file : s3client.listObjects(bucket, object).getObjectSummaries()) {
                s3client.deleteObject(bucket, file.getKey());
            }


        } catch (AmazonServiceException ase) {
            logAmazonServerExceptionInfo(ase);
        } catch (AmazonClientException ace) {
            logAmazonClientExceptionInfo(ace);
        }
    }

    private void logAmazonClientExceptionInfo(AmazonClientException ace) {
        log.info("Caught an AmazonClientException, which " +
                "means the client encountered " +
                "an internal error while trying to " +
                "communicate with S3, " +
                "such as not being able to access the network.");
        log.info("Error Message: " + ace.getMessage());
    }

    private void logAmazonServerExceptionInfo(AmazonServiceException ase) {
        log.info("Caught an AmazonServiceException, which " +
                "means your request made it " +
                "to Amazon S3, but was rejected with an error response" +
                " for some reason.");
        log.info("Error Message:    " + ase.getMessage());
        log.info("HTTP Status Code: " + ase.getStatusCode());
        log.info("AWS Error Code:   " + ase.getErrorCode());
        log.info("Error Type:       " + ase.getErrorType());
        log.info("Request ID:       " + ase.getRequestId());
    }

}
