package com.mapr.fs.dao.dto;

public class MonitorDTO {

    private String name;
    private String _id;
    private boolean monitoring;

    public boolean isMonitoring() {
        return monitoring;
    }

    public void setMonitoring(boolean monitoring) {
        this.monitoring = monitoring;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MonitorDTO that = (MonitorDTO) o;

        if (!name.equals(that.name)) return false;
        return _id.equals(that._id);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + _id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MonitorDTO{" +
                "name='" + name + '\'' +
                ", _id='" + _id + '\'' +
                ", monitoring=" + monitoring +
                '}';
    }
}
