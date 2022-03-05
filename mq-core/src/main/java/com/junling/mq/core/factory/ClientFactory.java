package com.junling.mq.core.factory;

import com.junling.mq.core.annotation.ConsumerBean;
import com.junling.mq.core.broker.IBroker;
import com.junling.mq.core.call.CallType;
import com.junling.mq.core.consumer.thread.ConsumerThread;
import com.junling.mq.core.consumer.IConsumer;
import com.junling.mq.core.loadbalancer.LoadBalancerType;
import com.junling.mq.core.net.NetType;
import com.junling.mq.core.rpc.factory.RpcInvokerFactory;
import com.junling.mq.core.rpc.registry.impl.AdminServiceRegistry;
import com.junling.mq.core.rpc.registry.model.MqReferenceBean;
import com.junling.mq.core.rpc.registry.model.RegistryParam;
import com.junling.mq.core.serializer.SerializerType;
import com.junling.mq.core.message.MqMessage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class ClientFactory {

    private String adminAddress;
    private List<ConsumerThread> consumerThreadList = new ArrayList<>();

    private LinkedBlockingDeque<MqMessage> messageQueue = new LinkedBlockingDeque<>();
    public static LinkedBlockingDeque<MqMessage> callbackMessageQueue = new LinkedBlockingDeque<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();
    public static boolean executorStop = false;

    public static RpcInvokerFactory rpcInvokerFactory;
    private static IBroker broker;

    public ClientFactory(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    public void setConsumers(List<IConsumer> consumerList) {
        for (IConsumer consumer: consumerList) {
            ConsumerBean annotation = consumer.getClass().getDeclaredAnnotation(ConsumerBean.class);
            String group = annotation.group();
            String topic = annotation.topic();

            if (group == null || group.trim().length() == 0) { //if group is empty, then generate uuid to group and broadcast it
                InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
                try {
                    Field field = invocationHandler.getClass().getDeclaredField("memberValue");
                    field.setAccessible(true);
                    Map memberValues = (Map)field.get(invocationHandler);
                    memberValues.put("group", UUID.randomUUID().toString());
                }
                catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            group = annotation.group();
            if (group == null || group.trim().length() == 0) {
                throw new RuntimeException("group is empty");
            }
            if (topic == null || topic.trim().length() == 0) {
                throw new RuntimeException("topic is empty");
            }
            consumerThreadList.add(new ConsumerThread(consumer));
        }
    }

    public void start() {
        startBroker();
        startConsumer();
    }

    private void startConsumer() {
        consumerRegistry();

        //execute consumer thread
        for (ConsumerThread consumerThread: consumerThreadList) {
            executorService.execute(consumerThread);
        }
    }

    private void consumerRegistry() {

        Set<RegistryParam> registryParamSet = new HashSet<>();

        for (ConsumerThread consumerThread: consumerThreadList) {
            ConsumerBean consumerAnnotation = consumerThread.getConsumerAnnotation();
            String group = consumerAnnotation.group();
            String topic = consumerAnnotation.topic();
            String uuid = consumerThread.getUuid();

            String registryKey = "consumer_" + topic;
            String registryVal = "consumer+" + group + "_" + uuid;
            registryParamSet.add(new RegistryParam(registryKey, registryVal));
        }

        rpcInvokerFactory.getServiceRegistry().registry(new ArrayList<>(registryParamSet));

    }


    private void startBroker() {
        rpcInvokerFactory = new RpcInvokerFactory(AdminServiceRegistry.class, adminAddress);
        rpcInvokerFactory.start();

        MqReferenceBean mqReferenceBean = new MqReferenceBean(
                NetType.netty.getNetClientClazz(),
                SerializerType.hessian.getSerializerClazz(),
                LoadBalancerType.round.getLoadBalancerClazz(),
                CallType.sync,
                IBroker.class,
                adminAddress,
                rpcInvokerFactory
        );

        broker = (IBroker) mqReferenceBean.getObject();

        for (int i= 0; i < 3; i++) {
            executorService.execute(()->{
                while (!executorStop) {
                    try {
                        MqMessage message = messageQueue.take();

                        if (message != null) {
                            List<MqMessage> messageList = new ArrayList<>();
                            messageQueue.drainTo(messageList, 100);
                            messageList.add(message);
                            broker.saveMessages(messageList);
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                List<MqMessage> messageList = new ArrayList<>();
                int count = messageQueue.drainTo(messageList);
                if (count > 0) {
                    broker.saveMessages(messageList);
                }
            });
        }

        for (int i = 0; i < 3; i++) {
            executorService.execute(()->{
                while (!executorStop) {
                    try {
                        MqMessage message = callbackMessageQueue.take();
                        if (message != null) {
                            List<MqMessage> messageList = new ArrayList<>();
                            callbackMessageQueue.drainTo(messageList, 100);
                            messageList.add(message);
                            broker.saveCallbackMessages(messageList);
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                List<MqMessage> messageList = new ArrayList<>();
                int count = callbackMessageQueue.drainTo(messageList);
                if (count > 0) {
                    broker.saveCallbackMessages(messageList);
                }
            });
        }

    }


    public static IBroker getBroker() {
        return broker;
    }
}
