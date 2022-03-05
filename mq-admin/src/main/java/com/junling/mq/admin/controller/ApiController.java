package com.junling.mq.admin.controller;

import com.alibaba.fastjson.JSON;
import com.junling.mq.admin.registry.RegistryData;
import com.junling.mq.admin.service.RegistryService;
import com.junling.mq.core.rpc.registry.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Controller
@RequestMapping ("/api")
public class ApiController {

    @Autowired
    private RegistryService registryService;

    @PostMapping("/registry")
    public R<String> registry(@RequestBody String data) {

        List<RegistryData> registryDataList = JSON.parseArray(data, RegistryData.class);
        return registryService.registry(registryDataList);

    }

    @PostMapping("/discovery")
    public R<Map<String, TreeSet<String>>> discovery(@RequestBody String data) {
        List<String> keyList = JSON.parseArray(data, String.class);
        return registryService.discovery(keyList);
    }

    @PostMapping("/monitor")
    public R<String> monitor(@RequestBody String data) {
        List<String> keyList = JSON.parseArray(data, String.class);
        return registryService.monitor(keyList);
    }
}
