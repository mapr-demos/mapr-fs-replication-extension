package com.mapr.fs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple bean containing all configuration information for the S3 plugin
 * The various information are coming from configuration file and DB
 */
@Data
@AllArgsConstructor(suppressConstructorProperties = true)
@NoArgsConstructor
public class PluginConfiguration {

    private String accessKey;
    private String secretKey;
    private String volumeName;
    private String volumePath;
    private String bucketName;
    private boolean createEnabled = false;
    private boolean deleteEnabled = false;
    private boolean modifyEnabled = false;
    private boolean renameEnabled = false;

}
