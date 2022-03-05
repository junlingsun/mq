package com.junling.mq.admin.registry;

public class RegistryMessage {

    private Integer id;
    private String data;
    private String updateTime;

    public Integer getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
