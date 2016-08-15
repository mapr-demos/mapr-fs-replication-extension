package com.mapr.fs.dao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"lastEvent","dateTime"})
public class FileStatusDto {

    private String filename;
    private String lastEvent;
    private LocalDateTime dateTime;

}
