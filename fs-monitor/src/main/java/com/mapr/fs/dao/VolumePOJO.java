package com.mapr.fs.dao;

public class VolumePOJO {
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
}
