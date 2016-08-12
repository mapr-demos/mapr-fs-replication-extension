package com.mapr.fs.s3;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple bean containing all configuration information for the S3 plugin
 * The various information are coming from configuratio file and DB
 */
public class PluginConfiguration {

  private String accessKey;
  private String secretKey;
  private String bucket;
  private Map<String,String> directories = new HashMap<String,String>();
  private boolean createEnabled = false;
  private boolean deleteEnabled = false;
  private boolean modifyEnabled = false;
  private boolean renameEnabled = false;

  public PluginConfiguration() {
  }

  public PluginConfiguration(String accessKey,
                             String secretKey,
                             String bucket,
                             Map<String, String> directories,
                             boolean createEnabled,
                             boolean deleteEnabled,
                             boolean modifyEnabled,
                             boolean renameEnabled) {
    this.secretKey = secretKey;
    this.accessKey = accessKey;
    this.bucket = bucket;
    this.directories = directories;
    this.createEnabled = createEnabled;
    this.deleteEnabled = deleteEnabled;
    this.modifyEnabled = modifyEnabled;
    this.renameEnabled = renameEnabled;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public Map<String, String> getDirectories() {
    return directories;
  }

  public void setDirectories(Map<String, String> directories) {
    this.directories = directories;
  }

  public boolean isCreateEnabled() {
    return createEnabled;
  }

  public void setCreateEnabled(boolean createEnabled) {
    this.createEnabled = createEnabled;
  }

  public boolean isDeleteEnabled() {
    return deleteEnabled;
  }

  public void setDeleteEnabled(boolean deleteEnabled) {
    this.deleteEnabled = deleteEnabled;
  }

  public boolean isModifyEnabled() {
    return modifyEnabled;
  }

  public void setModifyEnabled(boolean modifyEnabled) {
    this.modifyEnabled = modifyEnabled;
  }

  public boolean isRenameEnabled() {
    return renameEnabled;
  }

  public void setRenameEnabled(boolean renameEnabled) {
    this.renameEnabled = renameEnabled;
  }

  @Override
  public String toString() {
    return "PluginConfiguration : " +
            "\n\tsecretKey (is null?) = " + (secretKey==null)  +
            "\n\taccessKey (is null?) = " + (accessKey==null) +
            "\n\tbucket = " + bucket +
            "\n\tdirectories = " + directories +
            "\n\tcreateEnabled = " + createEnabled +
            "\n\tdeleteEnabled = " + deleteEnabled +
            "\n\tmodifyEnabled = " + modifyEnabled +
            "\n\trenameEnabled = " + renameEnabled
            ;
  }
}
