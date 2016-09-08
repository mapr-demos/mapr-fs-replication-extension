package com.mapr.fs.dao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor(suppressConstructorProperties = true)
@NoArgsConstructor
public class SourceDTO {

    @JsonProperty("_id")
    private String bucket;
    private Set<VolumeOfSourceDTO> volumes;
}
