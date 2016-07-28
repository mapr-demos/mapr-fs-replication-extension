package io.swagger.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


/**
 * The details about a single volume
 **/
@ApiModel(description = "The details about a single volume")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringBootServerCodegen", date = "2016-07-28T04:59:47.648Z")
public class Volume  {
  
  private String sourceCluster = null;
  private String name = null;
  private String localPath = null;
  private Boolean paused = null;
  private Integer secondsBehind = null;
  private Long lastChange = null;

  /**
   * The cluster this volume is replicated from
   **/
  @ApiModelProperty(value = "The cluster this volume is replicated from")
  @JsonProperty("source_cluster")
  public String getSourceCluster() {
    return sourceCluster;
  }
  public void setSourceCluster(String sourceCluster) {
    this.sourceCluster = sourceCluster;
  }

  /**
   * The name of the volume on the source side
   **/
  @ApiModelProperty(value = "The name of the volume on the source side")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Where the image of the volume should be placed
   **/
  @ApiModelProperty(value = "Where the image of the volume should be placed")
  @JsonProperty("local_path")
  public String getLocalPath() {
    return localPath;
  }
  public void setLocalPath(String localPath) {
    this.localPath = localPath;
  }

  /**
   * Set to true if the replication has been paused
   **/
  @ApiModelProperty(value = "Set to true if the replication has been paused")
  @JsonProperty("paused")
  public Boolean getPaused() {
    return paused;
  }
  public void setPaused(Boolean paused) {
    this.paused = paused;
  }

  /**
   * How far behind real-time this replica is
   **/
  @ApiModelProperty(value = "How far behind real-time this replica is")
  @JsonProperty("seconds_behind")
  public Integer getSecondsBehind() {
    return secondsBehind;
  }
  public void setSecondsBehind(Integer secondsBehind) {
    this.secondsBehind = secondsBehind;
  }

  /**
   * When was the last change event processed?
   **/
  @ApiModelProperty(value = "When was the last change event processed?")
  @JsonProperty("last_change")
  public Long getLastChange() {
    return lastChange;
  }
  public void setLastChange(Long lastChange) {
    this.lastChange = lastChange;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Volume volume = (Volume) o;
    return Objects.equals(sourceCluster, volume.sourceCluster) &&
        Objects.equals(name, volume.name) &&
        Objects.equals(localPath, volume.localPath) &&
        Objects.equals(paused, volume.paused) &&
        Objects.equals(secondsBehind, volume.secondsBehind) &&
        Objects.equals(lastChange, volume.lastChange);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceCluster, name, localPath, paused, secondsBehind, lastChange);
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Volume {\n");
    
    sb.append("  sourceCluster: ").append(sourceCluster).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  localPath: ").append(localPath).append("\n");
    sb.append("  paused: ").append(paused).append("\n");
    sb.append("  secondsBehind: ").append(secondsBehind).append("\n");
    sb.append("  lastChange: ").append(lastChange).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
