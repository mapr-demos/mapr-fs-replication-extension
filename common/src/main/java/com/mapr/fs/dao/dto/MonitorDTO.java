package com.mapr.fs.dao.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = "monitoring")
public class MonitorDTO {

    private String _id;
    private String name;
    private boolean monitoring;

}
