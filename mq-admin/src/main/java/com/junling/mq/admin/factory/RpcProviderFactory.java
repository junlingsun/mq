package com.junling.mq.admin.factory;

import com.junling.mq.admin.broker.Broker;
import com.junling.mq.core.net.client.NetClient;
import com.junling.mq.core.net.server.NetServer;
import com.junling.mq.core.serializer.Serializer;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class RpcProviderFactory {

    private String remoteHost;
    private int remotePort;
    private Class<? extends NetServer> netServerClazz;
    private Class<? extends Serializer> serializerClazz;
    private Map<String, Object> serviceParamMap = new HashMap<>();

    public RpcProviderFactory(String remoteHost, int remotePort, Class<? extends NetServer> netServerClazz, Class<? extends Serializer> serializerClazz) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
       this.netServerClazz = netServerClazz;
        this.serializerClazz = serializerClazz;
    }

    public void start() {
        try {
            NetServer netServer = netServerClazz.getDeclaredConstructor().newInstance();
            netServer.start(remoteHost, remotePort, serviceParamMap);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }


    }

    public void addServiceParam(String name, Broker broker) {

        serviceParamMap.put(name, broker);
    }
}
