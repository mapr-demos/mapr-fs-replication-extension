package com.mapr.fs.dao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"replicating"})
public class VolumeDTO {

    private String cluster_name;
    private String name;
    private String path;
    private boolean replicating;

}
