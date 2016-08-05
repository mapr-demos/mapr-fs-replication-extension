package com.mapr.fs.dao.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(exclude = "lastEvent")
public class FileStatusDto {

    private String filename;
    private String lastEvent;

}
