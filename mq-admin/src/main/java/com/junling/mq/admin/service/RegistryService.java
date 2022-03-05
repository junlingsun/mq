package com.junling.mq.admin.service;

import com.junling.mq.admin.registry.RegistryData;
import com.junling.mq.core.rpc.registry.model.R;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface RegistryService {
    R<String> registry(List<RegistryData> registryDataList);

    R<Map<String, TreeSet<String>>> discovery(List<String> keyList);

    R<String> monitor(List<String> keyList);
}
