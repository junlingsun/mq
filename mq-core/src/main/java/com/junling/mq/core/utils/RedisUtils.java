package com.junling.mq.core.utils;

public class RedisUtils {

    private static String prefix = "mq:registry:key:";

    public static String getRedisKey(String registryKey) {
        return prefix+registryKey;
    }

    public static String getRedisPrefix(){
        return prefix;
    }
}
