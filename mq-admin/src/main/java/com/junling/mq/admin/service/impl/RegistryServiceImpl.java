package com.junling.mq.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.junling.mq.admin.dao.RegistryDao;
import com.junling.mq.admin.dao.RegistryGroupDao;
import com.junling.mq.admin.dao.RegistryMessageDao;
import com.junling.mq.admin.registry.RegistryData;
import com.junling.mq.admin.registry.RegistryGroup;
import com.junling.mq.admin.registry.RegistryMessage;
import com.junling.mq.admin.service.RegistryService;
import com.junling.mq.core.rpc.registry.model.R;
import com.junling.mq.core.utils.RedisUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Service
public class RegistryServiceImpl implements RegistryService, InitializingBean {
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private boolean threadStop = false;

    private LinkedBlockingDeque<RegistryData> registryQueue = new LinkedBlockingDeque<>();

    private volatile List<Integer> messageIds = new ArrayList<>();
    private static RegistryData staticRegistryData;

    private Map<String, List<DeferredResult>> deferredMap = new HashMap<>();

    @Autowired
    private RegistryDao registryDao;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RegistryGroupDao registryGroupDao;

    @Autowired
    private RegistryMessageDao registryMessageDao;

    @Value("${registry.beat-time}")
    private int beatTime;

    @Override
    public R<String> registry(List<RegistryData> registryDataList) {
        registryQueue.addAll(registryDataList);

        return new R<String>(200, "registry is success", null);
    }

    @Override
    public R<Map<String,TreeSet<String>>> discovery(List<String> keyList) {
        Map<String, TreeSet<String>> map = new HashMap<>();
        for (String key: keyList) {
            String redisKey = RedisUtils.getRedisKey(key);
            String data = redisTemplate.opsForValue().get(redisKey);
            List<String> values = JSON.parseArray(data, String.class);
            TreeSet<String> set = new TreeSet<>();
            set.addAll(values);
            map.put(key, set);
        }

        return new R(200,"discovery successfully", map);
    }

    @Override
    public R<String> monitor(List<String> keyList) {
        DeferredResult<R<String>> deferredResult = new DeferredResult<>(beatTime*3L, new R(200, "monitor time out",null));
        for (String key: keyList) {
            if (!deferredMap.containsKey(key)) {
                deferredMap.put(key, new ArrayList<>());
            }

            deferredMap.get(key).add(deferredResult);
        }

        R<String> result = (R<String>)deferredResult.getResult();
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        //register threads
        executorService.execute(()->{
            while (!threadStop) {
                try {
                    RegistryData registryData = registryQueue.take();
                    if (registryData != null) {
                        int result = registryDao.update(registryData);

                        if (result == 0) {
                            registryDao.save(registryData);
                        }

                        String registryKey = registryData.getRegistryKey();
                        String redisKey = RedisUtils.getRedisKey(registryKey);
                        if (!redisTemplate.hasKey(redisKey)) {
                            String data = redisTemplate.opsForValue().get(redisKey);
                            List<String> values = JSON.parseArray(data, String.class);
                            if (values.contains(registryData.getRegistryVal())) continue;
                        }

                        sendMessage(registryData);

                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        //handle messages and update redis
        executorService.execute(()->{
            while (!threadStop) {
                List<RegistryMessage> messageList = registryMessageDao.findAll(messageIds);
                if (messageList != null && messageList.size() > 0) {
                    for (RegistryMessage message: messageList) {
                        String data = message.getData();
                        RegistryGroup registryGroup = JSON.parseObject(data, RegistryGroup.class);
                        String registryKey = registryGroup.getRegistryKey();
                        String redisKey = RedisUtils.getRedisKey(registryKey);
                        redisTemplate.opsForValue().set(redisKey, registryGroup.getData());

                        messageIds.add(message.getId());
                    }
                }

                if (System.currentTimeMillis() % beatTime == 0) {
                    registryMessageDao.clean(beatTime);
                    messageIds.clear();
                }

                try {
                    TimeUnit.SECONDS.sleep(1);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        });

        //register services and clean old data
        executorService.execute(()->{
            while (!threadStop) {
                if (staticRegistryData != null) {
                    registryQueue.add(staticRegistryData);
                }

                registryDao.clean(beatTime * 3);
                registryGroupDao.cleanInactive();

                int offset = 0;
                int pageSize = 1000;
                Set<String> updatedKeySet = new HashSet<>();

                List<RegistryGroup> registryGroupList = registryGroupDao.pageList(offset, pageSize);
                while (registryGroupList != null && registryGroupList.size() > 0) {
                    for (RegistryGroup registryGroup: registryGroupList) {
                        String registryKey = registryGroup.getRegistryKey();
                        List<RegistryData> registryDataList = registryDao.findAll(registryKey);
                        List<String> values = new ArrayList<>();

                        if (registryDataList == null || registryDataList.size() == 0) continue;
                        for (RegistryData registryData: registryDataList) {
                            values.add(registryData.getRegistryVal());
                        }
                        String data = JSON.toJSONString(values);
                        if (!registryGroup.getData().equals(data)) {
                            registryGroup.setData(data);
                            registryGroupDao.update(registryGroup);
                        }

                        syncRedis(registryGroup);
                        updatedKeySet.add(registryGroup.getRegistryKey());
                    }

                    offset += pageSize;
                    registryGroupList = registryGroupDao.pageList(offset, pageSize);
                }

                //delete all inactive services
                refreshRedis(updatedKeySet);
            }
        });
    }

    private void refreshRedis(Set<String> updatedKeyList) {
        Set<String> keys = redisTemplate.keys(RedisUtils.getRedisPrefix() + "*");
        for (String key: keys) {
            if (!updatedKeyList.contains(key)) {
                redisTemplate.delete(key);
            }
        }

    }

    private void syncRedis(RegistryGroup registryGroup) {

        //sync redis
        String registryKey = registryGroup.getRegistryKey();
        String redisKey = RedisUtils.getRedisKey(registryKey);
        redisTemplate.opsForValue().set(redisKey, registryGroup.getData());

        //everytime redis got refreshed, then handle corresponding deferredResult
        List<DeferredResult> deferredResultList = deferredMap.get(redisKey);
        if (deferredResultList != null && deferredResultList.size()>0) {
            for (DeferredResult deferredResult: deferredResultList) {
                deferredResult.setResult(new R(200, "monitor key update", null));
            }
        }


    }

    private void sendMessage(RegistryData registryData) {
        String registryKey = registryData.getRegistryKey();

        List<RegistryData> registryDataList = registryDao.findAll(registryKey);
        List<String> values = new ArrayList<>();
        for (RegistryData data: registryDataList) {
            values.add(data.getRegistryVal());
        }
        String data = JSON.toJSONString(values);

        RegistryGroup registryGroup = registryGroupDao.find(registryKey);

        boolean sendMsg = false;

        if (registryGroup == null) {
            RegistryGroup group = new RegistryGroup(registryKey, data);
            registryGroupDao.save(group);
            sendMsg = true;
        }else {
            if (registryGroup.getData() != data) {
                registryGroup.setData(data);
                registryGroupDao.update(registryGroup);
                sendMsg = true;
            }
        }

        if (sendMsg) {
            forwardMessage(registryGroup);
        }
    }

    private void forwardMessage(RegistryGroup registryGroup) {
        RegistryMessage registryMessage = new RegistryMessage();
        String data = JSON.toJSONString(registryGroup);
        registryMessage.setData(data);
        registryMessageDao.save(registryMessage);

    }
}
