package com.mapr.fs.dao;

import java.util.List;

public class ClusterPOJO {
    private String clusterName;
    private List<VolumePOJO> volumes;
    private String _id;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<VolumePOJO> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<VolumePOJO> volumes) {
        this.volumes = volumes;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }
}
