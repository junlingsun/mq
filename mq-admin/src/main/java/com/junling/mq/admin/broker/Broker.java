package com.junling.mq.admin.broker;

import com.junling.mq.admin.factory.RpcProviderFactory;
import com.junling.mq.admin.service.MessageService;
import com.junling.mq.core.broker.IBroker;
import com.junling.mq.core.message.MqMessage;
import com.junling.mq.core.net.NetType;
import com.junling.mq.core.serializer.SerializerType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class Broker implements IBroker, InitializingBean {

    @Autowired
    private MessageService messageService;

    private String remoteHost;
    private int remotePort;

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private boolean threadStop = false;

    private static LinkedBlockingDeque<MqMessage> messageQueue = new LinkedBlockingDeque<>();
    private static LinkedBlockingDeque<MqMessage> callbackMessageQueue = new LinkedBlockingDeque<>();

    public Broker(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        startServer();

        startThreads();
    }

    private void startThreads() {

        //handle messagequeue
        for (int i = 0; i < 3; i++) {
            executorService.execute(()->{
                while(!threadStop) {
                    try {
                        MqMessage message = messageQueue.take();
                        if (message != null) {
                            List<MqMessage> messageList = new ArrayList<>();
                            messageQueue.drainTo(messageList, 100);
                            messageList.add(message);
                            messageService.saveAll(messageList);
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                List<MqMessage> messageList = new ArrayList<>();
                int count = messageQueue.drainTo(messageList);
                if (count > 0) {
                    messageService.saveAll(messageList);
                }
            });
        }

        //handle callback messages
        for (int i = 0; i < 3; i++) {
            executorService.execute(()->{
                while(!threadStop) {
                    try {
                        MqMessage message = callbackMessageQueue.take();
                        if (message != null) {
                            List<MqMessage> messageList = new ArrayList<>();
                            callbackMessageQueue.drainTo(messageList,100);
                            messageList.add(message);
                            messageService.updateAll(messageList);
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                List<MqMessage> messageList = new ArrayList<>();
                int count = callbackMessageQueue.drainTo(messageList);
                if (count > 0) {
                    messageService.updateAll(messageList);
                }

            });
        }

        //


    }

    private void startServer() {

        startRegistry();
        startNet();



    }

    private void startRegistry() {

    }

    private void startNet() {
        String remoteAddress = remoteHost + ":" + remotePort;
        RpcProviderFactory rpcProviderFactory = new RpcProviderFactory(remoteHost, remotePort, NetType.netty.getNetServerClazz(), SerializerType.hessian.getSerializerClazz());
        rpcProviderFactory.addServiceParam(IBroker.class.getName(), this);
        rpcProviderFactory.start();
    }

    @Override
    public void saveMessages(List<MqMessage> messageList) {
        messageQueue.addAll(messageList);

    }

    @Override
    public void saveCallbackMessages(List<MqMessage> messageList) {
        callbackMessageQueue.addAll(messageList);

    }

    @Override
    public List<MqMessage> pullMessages(String group, String topic, int rank, int total, int count) {
        return messageService.pullMessages(group, topic, rank, total, count);

    }
}
