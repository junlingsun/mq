package com.junling.mq.core.rpc.registry;

import com.junling.mq.core.rpc.registry.model.RegistryParam;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;

public interface ServiceRegistry {
    void init(String registryAddress);

    Map<String, TreeSet<String>> discovery(String registryKey);

    boolean registry(ArrayList<RegistryParam> registryParams);
}
