package com.mapr.fs.dao.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(exclude = {"lastEvent", "dateTime"})
public class FileStatusDto {

    private String filename;
    private String lastEvent;
    private String dateTime;


    @JsonCreator
    public FileStatusDto(@JsonProperty("filename") String filename, @JsonProperty("lastEvent") String lastEvent, @JsonProperty("dateTime") String dateTime) {
        this.filename = filename;
        this.lastEvent = lastEvent;
        this.dateTime = dateTime;
    }

}