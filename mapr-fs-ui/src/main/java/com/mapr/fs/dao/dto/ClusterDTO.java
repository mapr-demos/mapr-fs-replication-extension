package com.mapr.fs.dao.dto;

import java.util.Set;

public class ClusterDTO {

    private String cluster_name;
    private Set<VolumeDTO> volumes;
    private String _id;

    public String getCluster_name() {
        return cluster_name;
    }

    public void setCluster_name(String cluster_name) {
        this.cluster_name = cluster_name;
    }

    public Set<VolumeDTO> getVolumes() {
        return volumes;
    }

    public void setVolumes(Set<VolumeDTO> volumes) {
        this.volumes = volumes;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }
}
