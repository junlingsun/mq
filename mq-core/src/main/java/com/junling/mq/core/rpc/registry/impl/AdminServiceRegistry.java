package com.junling.mq.core.rpc.registry.impl;

import com.junling.mq.core.rpc.registry.RegistryClient;
import com.junling.mq.core.rpc.registry.ServiceRegistry;
import com.junling.mq.core.rpc.registry.model.RegistryParam;

import java.util.*;

public class AdminServiceRegistry implements ServiceRegistry {

    private RegistryClient registryClient;

    @Override
    public void init(String registryAddress) {

        registryClient = new RegistryClient(registryAddress);

    }

    @Override
    public Map<String,TreeSet<String>> discovery(String registryKey) {
        Set<String> keySet = new HashSet<>();
        keySet.add(registryKey);
        return registryClient.discovery(keySet);
    }

    @Override
    public boolean registry(ArrayList<RegistryParam> registryParams) {
        return registryClient.registry(registryParams);
    }
}
