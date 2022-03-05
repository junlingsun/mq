package com.junling.mq.core.rpc.factory;

import com.junling.mq.core.model.FutureRpcResponse;
import com.junling.mq.core.model.RpcResponse;
import com.junling.mq.core.rpc.registry.ServiceRegistry;
import com.junling.mq.core.rpc.registry.impl.AdminServiceRegistry;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

public class RpcInvokerFactory {

    private String registryAddress;
    private ServiceRegistry serviceRegistry;
    private Class<? extends ServiceRegistry> serviceRegistryClass;

    private static volatile ConcurrentHashMap<String, FutureRpcResponse> futureRespMap = new ConcurrentHashMap<>();

    public RpcInvokerFactory(Class<AdminServiceRegistry> serviceRegistryClass, String registryAddress) {
        this.registryAddress = registryAddress;
        this.serviceRegistryClass = serviceRegistryClass;
    }

    public void start() {
        try {
           serviceRegistry = serviceRegistryClass.getDeclaredConstructor().newInstance();
            serviceRegistry.init(registryAddress);

        }
        catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public void notify(String uuid, RpcResponse rpcResponse) {

        FutureRpcResponse futureRpcResponse = futureRespMap.get(uuid);
        if (futureRpcResponse == null) {
            return;
        }

        futureRpcResponse.setResponse(rpcResponse);
        futureRespMap.remove(uuid);






    }

    public void setFuture(String uuid, FutureRpcResponse futureRpcResponse) {
        futureRespMap.put(uuid, futureRpcResponse);
    }
}
