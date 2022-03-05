package com.junling.mq.core.rpc.registry.model;

public class RegistryParam {

    private String registryKey;
    private String registryVal;

    public RegistryParam(String registryKey, String registryVal) {
        this.registryKey = registryKey;
        this.registryVal = registryVal;
    }

    public void setRegistryKey(String registryKey) {
        this.registryKey = registryKey;
    }

    public void setRegistryVal(String registryVal) {
        this.registryVal = registryVal;
    }

    public String getRegistryKey() {
        return registryKey;
    }

    public String getRegistryVal() {
        return registryVal;
    }
}
