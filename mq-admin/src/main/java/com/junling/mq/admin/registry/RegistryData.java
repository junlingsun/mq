package com.junling.mq.admin.registry;

import java.util.Date;

public class RegistryData {

    private Long id;
    private String registryKey;
    private String registryVal;
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegistryKey() {
        return registryKey;
    }

    public void setRegistryKey(String registryKey) {
        this.registryKey = registryKey;
    }

    public String getRegistryVal() {
        return registryVal;
    }

    public void setRegistryVal(String registryVal) {
        this.registryVal = registryVal;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
