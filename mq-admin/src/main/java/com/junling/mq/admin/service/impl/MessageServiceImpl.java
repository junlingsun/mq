package com.junling.mq.admin.service.impl;

import com.junling.mq.admin.dao.MqMessageDao;
import com.junling.mq.admin.service.MessageService;
import com.junling.mq.core.message.MqMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MqMessageDao messageDao;
    @Override
    public List<MqMessage> pullMessages(String group, String topic, int rank, int total, int count) {

        return messageDao.pullMessages(group, topic, rank, total, count);
    }

    @Override
    public void saveAll(List<MqMessage> messageList) {
        messageDao.saveAll(messageList);

    }

    @Override
    public void updateAll(List<MqMessage> messageList) {
        messageDao.updateAll(messageList);

    }
}
