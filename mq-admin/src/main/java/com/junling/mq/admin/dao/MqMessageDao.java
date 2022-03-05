package com.junling.mq.admin.dao;

import com.junling.mq.core.message.MqMessage;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MqMessageDao {
    List<MqMessage> pullMessages(@Param("group") String group, @Param("topic") String topic, @Param("rank") int rank, @Param("total") int total, @Param("count") int count);

    void saveAll(List<MqMessage> messageList);

    void updateAll(List<MqMessage> messageList);
}
