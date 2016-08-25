package com.mapr.fs.dao.dto;

public class VolumeDTO {

    private String cluster_name;
    private String name;
    private String path;
    private boolean replicating;

    public String getCluster_name() {
        return cluster_name;
    }

    public void setCluster_name(String cluster_name) {
        this.cluster_name = cluster_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReplicating() {
        return replicating;
    }

    public void setReplicating(boolean replicating) {
        this.replicating = replicating;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VolumeDTO dto = (VolumeDTO) o;

        if (!cluster_name.equals(dto.cluster_name)) return false;
        if (!name.equals(dto.name)) return false;
        return path.equals(dto.path);

    }

    @Override
    public int hashCode() {
        int result = cluster_name.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "VolumeDTO{" +
                "cluster_name='" + cluster_name + '\'' +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", replicating=" + replicating +
                '}';
    }
}
