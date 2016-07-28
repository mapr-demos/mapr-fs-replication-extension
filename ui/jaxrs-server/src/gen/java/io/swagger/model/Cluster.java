package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.model.Volume;
import java.util.ArrayList;
import java.util.List;



/**
 * A description of a cluster, including any replicated volumes
 **/

@ApiModel(description = "A description of a cluster, including any replicated volumes")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-07-28T04:59:33.824Z")
public class Cluster   {
  
  private String name = null;
  private List<Volume> volumes = new ArrayList<Volume>();

  /**
   * The name of the cluster
   **/
  public Cluster name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "The name of the cluster")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * The volumes from this cluster being replicated
   **/
  public Cluster volumes(List<Volume> volumes) {
    this.volumes = volumes;
    return this;
  }

  
  @ApiModelProperty(value = "The volumes from this cluster being replicated")
  @JsonProperty("volumes")
  public List<Volume> getVolumes() {
    return volumes;
  }
  public void setVolumes(List<Volume> volumes) {
    this.volumes = volumes;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Cluster cluster = (Cluster) o;
    return Objects.equals(name, cluster.name) &&
        Objects.equals(volumes, cluster.volumes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, volumes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Cluster {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    volumes: ").append(toIndentedString(volumes)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

