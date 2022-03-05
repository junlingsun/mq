package com.junling.mq.core.rpc.registry.model;

import com.junling.mq.core.call.CallType;
import com.junling.mq.core.loadbalancer.LoadBalancer;
import com.junling.mq.core.net.client.NetClient;
import com.junling.mq.core.rpc.factory.RpcInvokerFactory;
import com.junling.mq.core.model.FutureRpcResponse;
import com.junling.mq.core.model.RpcRequest;
import com.junling.mq.core.model.RpcResponse;
import com.junling.mq.core.rpc.registry.ServiceRegistry;
import com.junling.mq.core.serializer.Serializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.TreeSet;
import java.util.UUID;

public class MqReferenceBean {

    private Class<? extends NetClient> netClientClazz;
    private Class<? extends Serializer> serializerClazz;
    private Class<? extends LoadBalancer> loadbalancerClazz;
    private CallType callType;
    private Class<?> iface;

    private String address;
    private RpcInvokerFactory invokerFactory;

    private NetClient netClient;


    public MqReferenceBean(Class<? extends NetClient> netClientClazz, Class<? extends Serializer> serializerClazz, Class<? extends LoadBalancer> loadbalancerClazz, CallType callType, Class<?> iface, String address, RpcInvokerFactory invokerFactory) {
        this.netClientClazz = netClientClazz;
        this.serializerClazz = serializerClazz;
        this.loadbalancerClazz = loadbalancerClazz;
        this.callType = callType;
        this.iface = iface;
        this.address = address;
        this.invokerFactory = invokerFactory;
        try {
            this.netClient = netClientClazz.getDeclaredConstructor().newInstance();
        }
        catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public Object getObject() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{iface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String className = method.getDeclaringClass().getName();
                String methodName = method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();
                Object[] parameters = args;
                String uuid = UUID.randomUUID().toString();
                RpcRequest rpcRequest = new RpcRequest(
                        uuid,
                        className,
                        methodName,
                        parameterTypes,
                        parameters
                );

                String registryKey = iface.getName();
                String remoteAddress = null;

                ServiceRegistry serviceRegistry = invokerFactory.getServiceRegistry();
                TreeSet<String> addressSet = serviceRegistry.discovery(registryKey);

                if (!addressSet.isEmpty()) {
                    remoteAddress = addressSet.first();//TODO: implement loadbalancer
                }
                if (remoteAddress == null) {
                    throw new RuntimeException("The address of the bean " + iface.getName() + " is empty");
                }

                Serializer serializer = serializerClazz.getDeclaredConstructor().newInstance();

                if (callType == CallType.sync) {
                    FutureRpcResponse futureRpcResponse = new FutureRpcResponse(rpcRequest.getUuid(), invokerFactory);

                    netClient.asyncSend(remoteAddress, rpcRequest, serializer, invokerFactory);

                    RpcResponse response = (RpcResponse) futureRpcResponse.get();

                    if (response.getCode() != 200) {
                        throw new RuntimeException(response.getMsg());
                    }
                    return response.getData();


                }else return null;

                //TODO: implement other call type
            }
        });
    }
}
