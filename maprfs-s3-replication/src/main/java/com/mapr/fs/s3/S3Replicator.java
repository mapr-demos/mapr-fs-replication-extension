package com.mapr.fs.s3;

import com.mapr.fs.Config;
import com.mapr.fs.dao.ClusterDAO;
import com.mapr.fs.dao.dto.VolumeDTO;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class S3Replicator {

  private static final Logger log = Logger.getLogger(S3Replicator.class);
  private static PluginConfiguration pluginConfiguration;

  public static void main(String[] args) throws Exception {
    // Load configuration for S3 : bucket, volumes, ....
    // TODO : move from configuration to DB+API
    BasicConfigurator.configure();
    Config conf = new Config("s3.");
    Properties properties = conf.getProperties();

    String accessKey = properties.getProperty("aws_access_key_id");
    String secretKey = properties.getProperty("aws_secret_access_key");


    String s3Bucket = properties.getProperty("bucket");

    // load the list of directory with their name
    Map<String,String> directories = new HashMap<String,String>();
    String dirs = properties.getProperty("directories");
    for(String keyValue : dirs.split(",")) {
      String[] pairs = keyValue.split(":");
      directories.put(pairs[0],pairs[1]);
    }

    boolean createEnabled = Boolean.parseBoolean(properties.getProperty("operation.create", "false"));
    boolean modifyEnabled = Boolean.parseBoolean(properties.getProperty("operation.modify", "false"));
    boolean deleteEnabled = Boolean.parseBoolean(properties.getProperty("operation.delete", "false"));
    boolean renameEnabled = Boolean.parseBoolean(properties.getProperty("operation.rename", "false"));

    pluginConfiguration = new PluginConfiguration(accessKey, secretKey, s3Bucket, directories, createEnabled, deleteEnabled, modifyEnabled, renameEnabled);
    log.info("S3 Plugin Configuration "+ pluginConfiguration);

    startConsuming();
  }

  private static void startConsuming() throws IOException, InterruptedException {
    Set<String> volumes = Collections.synchronizedSet(new HashSet<String>());
    ExecutorService service = Executors.newCachedThreadPool();

    for (Map.Entry<String, String> entry : pluginConfiguration.getDirectories().entrySet()) {
      volumes.add(entry.getKey());
      service.submit(() -> {
        try {
          new Gateway( entry.getKey() , entry.getValue(), volumes, pluginConfiguration).processEvents();
        } catch (IOException e) {
          log.error("Cannot create Gateway"+ e.getMessage());
        }
      });
    }
  }



}
