package com.mapr.fs.dao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"lastEvent","dateTime"})
public class FileStatusDto {

    private String filename;
    private String lastEvent;
    private LocalDateTime dateTime;

}
