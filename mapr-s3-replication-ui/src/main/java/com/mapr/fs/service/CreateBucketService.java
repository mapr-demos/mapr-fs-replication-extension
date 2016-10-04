package com.mapr.fs.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.mapr.fs.Config;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

import static com.mapr.fs.utils.AmazonS3Util.logAmazonClientExceptionInfo;
import static com.mapr.fs.utils.AmazonS3Util.logAmazonServerExceptionInfo;

@Slf4j
public class CreateBucketService {

    private AmazonS3 s3client;

    public CreateBucketService() {

        Config conf = new Config("s3.");
        Properties properties = conf.getProperties();

        String accessKey = properties.getProperty("aws_access_key_id");
        String secretKey = properties.getProperty("aws_secret_access_key");

        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        this.s3client = new AmazonS3Client(credentials);
    }

    public void createBucket(String bucketName) {

        try {
            if (!(s3client.doesBucketExist(bucketName))) {
                s3client.createBucket(new CreateBucketRequest(
                        bucketName));
                log.info("Bucket created: " + bucketName);
            }

        } catch (AmazonServiceException ase) {
            logAmazonServerExceptionInfo(ase);
        } catch (AmazonClientException ace) {
            logAmazonClientExceptionInfo(ace);
        }
    }
}
