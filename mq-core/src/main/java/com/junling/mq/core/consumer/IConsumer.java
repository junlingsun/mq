package com.junling.mq.core.consumer;

import com.junling.mq.core.message.MqMessage;

public interface IConsumer {
    boolean consume(MqMessage message);
}
