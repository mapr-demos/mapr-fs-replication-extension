package com.mapr.fs.utils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AmazonS3Util {

    public static void logAmazonClientExceptionInfo(AmazonClientException ace) {
        log.info("Caught an AmazonClientException, which " +
                "means the client encountered " +
                "an internal error while trying to " +
                "communicate with S3, " +
                "such as not being able to access the network.");
        log.info("Error Message: " + ace.getMessage());
    }

    public static void logAmazonServerExceptionInfo(AmazonServiceException ase) {
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
