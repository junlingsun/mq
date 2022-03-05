package com.junling.mq.core.consumer.thread;

import com.junling.mq.core.annotation.ConsumerBean;
import com.junling.mq.core.broker.IBroker;
import com.junling.mq.core.consumer.IConsumer;
import com.junling.mq.core.factory.ClientFactory;
import com.junling.mq.core.rpc.factory.RpcInvokerFactory;
import com.junling.mq.core.message.MqMessage;

import java.util.*;

public class ConsumerThread implements Runnable{
    private IConsumer consumerIface;
    private ConsumerBean consumerAnnotation;
    private String uuid;


    public ConsumerThread(IConsumer consumerIface) {
        this.consumerIface = consumerIface;
        this.consumerAnnotation = consumerIface.getClass().getDeclaredAnnotation(ConsumerBean.class);
        this.uuid = UUID.randomUUID().toString();
    }


    public void setConsumerIface(IConsumer consumerIface) {
        this.consumerIface = consumerIface;
    }

    public void setConsumerAnnotation(ConsumerBean consumerAnnotation) {
        this.consumerAnnotation = consumerAnnotation;
    }

    public IConsumer getConsumerIface() {
        return consumerIface;
    }

    public ConsumerBean getConsumerAnnotation() {
        return consumerAnnotation;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public void run() {

        IBroker broker = ClientFactory.getBroker();

        String group = consumerAnnotation.group();
        String topic = consumerAnnotation.topic();

        while (!ClientFactory.executorStop) {

            int[] rest = findRank();
            if (rest == null || rest.length ==0) {
                int rank = rest[0];
                int total = rest[1];

                List<MqMessage> messageList = broker.pullMessages(group, topic, rank, total, 100);
                if (messageList != null && messageList.size() > 0) {
                    for (MqMessage message: messageList) {
                        boolean consumed = consumerIface.consume(message);
                        if (consumed) {
                            message.setStatus("SUCCESS");
                        }else {
                            message.setStatus("FAIL");
                        }
                        ClientFactory.callbackMessageQueue.add(message);
                    }
                }
            }
        }
    }

    private int[] findRank() {

        int[] rest = new int[2];

        int rank = -1;
        String group = consumerAnnotation.group();
        String topic = consumerAnnotation.topic();
        RpcInvokerFactory rpcInvokerFactory = ClientFactory.rpcInvokerFactory;

        String registryKey = "consumer_" + topic;
        String registryValPrefix = "consumer+" + group;
        String registryVal = "consumer+" + group + "_" + uuid;
        Map<String, TreeSet<String>> registryMap = rpcInvokerFactory.getServiceRegistry().discovery(registryKey);
        if (registryMap == null || registryMap.size() == 0) return null;

        Set<String> groupSet = new HashSet<>();
        for (String key: registryMap.keySet()){
            for (String val: registryMap.get(key)) {
                if (val.startsWith(registryValPrefix)) {
                    groupSet.add(val);
                }
            }

        }

        if (groupSet.isEmpty()) return null;
        rest[1] = groupSet.size();
        for (String val: groupSet) {
            rank++;
            if (val.equals(registryVal)) {
                break;
            }
        }
        rest[0] = rank;

        return rest;
    }
}
