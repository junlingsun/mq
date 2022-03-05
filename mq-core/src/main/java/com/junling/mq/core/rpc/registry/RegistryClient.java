package com.junling.mq.core.rpc.registry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.junling.mq.core.rpc.registry.model.R;
import com.junling.mq.core.rpc.registry.model.RegistryParam;
import com.junling.mq.core.utils.HttpUtils;
import com.junling.mq.core.utils.UrlUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RegistryClient {
    private String registryAddress;
    private Set<RegistryParam> registryParamSet = new HashSet<>();
    private List<String> registryAddressList = new ArrayList<>();
    private ConcurrentHashMap<String, TreeSet<String>> discoveryMap = new ConcurrentHashMap<>();
    
    private Thread registryThread;
    private Thread discoveryThread;
    private boolean threadStop = false;

    @Value("${registry.beat-time}")
    private int beatTime;

    public RegistryClient(String registryAddress) {
        this.registryAddress = registryAddress;

        String[] arr = this.registryAddress.split(",");
        registryAddressList.addAll(Arrays.asList(arr));
        
        //register services
        registryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadStop) {
                    
                    if (registryParamSet != null && registryParamSet.size() > 0) {
                        registry(new ArrayList<>(registryParamSet));
                    }

                    try {
                        TimeUnit.SECONDS.sleep(10L);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        registryThread.setDaemon(true);
        registryThread.start();

        //discover services
        discoveryThread = new Thread(()->{
            while (!threadStop) {

                if (discoveryMap.size() == 0) {
                    try {
                        TimeUnit.SECONDS.sleep(3L);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else {
                    boolean monitorValid = monitor(discoveryMap.keySet());

                    if (!monitorValid) {
                        try {
                            TimeUnit.SECONDS.sleep(10L);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    refreshDiscoveryData(discoveryMap.keySet());
                }
            }
        });

    }

    private void refreshDiscoveryData(Set<String> keySet) {

        if (keySet == null || keySet.size() == 0) return;

        Map<String, TreeSet<String>> discovery = discovery(keySet);
        for (String key: discovery.keySet()) {
            TreeSet<String> oldDataSet = discoveryMap.get(key);
            TreeSet<String> newDataSet = discovery.get(key);
            if (!oldDataSet.equals(newDataSet)) {
                discoveryMap.put(key, newDataSet);
            }

        }
    }

    private boolean monitor(Set<String> keySet) {

        String data = JSON.toJSONString(keySet);
        String url = UrlUtils.getURL(registryAddress, "api/monitor");
        String result = HttpUtils.post(data, url, 60);
        R resp = JSON.parseObject(result, R.class);
        return resp.getCode() == 200;


    }

    public boolean registry(ArrayList<RegistryParam> registryParams) {

        String jsonData = JSON.toJSONString(registryParams);
        String url = UrlUtils.getURL(registryAddress, "api/registry");
        String result = HttpUtils.post(jsonData, url, 5);
        R resp = JSON.parseObject(result, R.class);
        return resp.getCode() == 200;
    }

    public Map<String, TreeSet<String>> discovery(Set<String> keySet) {
        String jsonData = JSON.toJSONString(keySet);
        String url = UrlUtils.getURL(registryAddress, "api/discovery");
        String result = HttpUtils.post(jsonData, url, 5);
        R resp = JSON.parseObject(result, R.class);
        Map<String, TreeSet<String>> data = JSON.parseObject(resp.getData().toString(), new TypeReference<Map<String,TreeSet<String>>>(){});
        return data;
    }
}
