package com.junling.mq.consumer.consumer.impl;

import com.junling.mq.core.annotation.ConsumerBean;
import com.junling.mq.core.consumer.IConsumer;
import com.junling.mq.core.message.MqMessage;

@ConsumerBean (topic = "topic")
public class ConsumerDemo implements IConsumer {

    @Override
    public boolean consume(MqMessage message) {
        System.out.println("message " + message + " is consumed");
        return true;
    }
}
