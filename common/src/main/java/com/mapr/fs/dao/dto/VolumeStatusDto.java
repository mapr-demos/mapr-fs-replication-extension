package com.mapr.fs.dao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Set;


@Data
public class VolumeStatusDto {

    @JsonProperty("_id")
    private String volumeName;
    private Set<FileStatusDto> files;


}
