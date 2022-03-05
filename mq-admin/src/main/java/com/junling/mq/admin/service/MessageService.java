package com.junling.mq.admin.service;

import com.junling.mq.core.message.MqMessage;

import java.util.List;

public interface MessageService {
    List<MqMessage> pullMessages(String group, String topic, int rank, int total, int count);

    void saveAll(List<MqMessage> messageList);

    void updateAll(List<MqMessage> messageList);
}
