package com.mapr.fs.dao.dto;

public class VolumeDTO {

    private String name;
    private boolean replicating;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VolumeDTO volumeDTO = (VolumeDTO) o;

        return name.equals(volumeDTO.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
