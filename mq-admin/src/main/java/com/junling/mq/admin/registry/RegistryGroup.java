package com.junling.mq.admin.registry;

public class RegistryGroup {

    private Long id;
    private String registryKey;
    private String data;

    public RegistryGroup(String registryKey, String data) {
        this.registryKey = registryKey;
        this.data = data;
    }

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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
