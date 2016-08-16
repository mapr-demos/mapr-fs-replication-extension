package com.mapr.fs.dao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;


@Data
@AllArgsConstructor(suppressConstructorProperties = true)
@NoArgsConstructor
public class VolumeStatusDto {

    @JsonProperty("_id")
    private String volumeName;
    private LinkedHashSet<FileStatusDto> files;

}
