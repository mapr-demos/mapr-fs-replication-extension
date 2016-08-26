package com.mapr.fs.dao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterDTO {

    private String _id;
    private String cluster_name;
    private Set<VolumeDTO> volumes;

}
