package com.junling.mq.core.broker;

import com.junling.mq.core.message.MqMessage;

import java.util.List;

public interface IBroker {

    void saveMessages(List<MqMessage> messageList);

    void saveCallbackMessages(List<MqMessage> messageList);

    List<MqMessage> pullMessages(String group, String topic, int rank, int total, int i);
}
