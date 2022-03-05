package com.junling.mq.admin.dao;

import com.junling.mq.admin.registry.RegistryMessage;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RegistryMessageDao {
    void save(@Param("message") RegistryMessage registryMessage);

    List<RegistryMessage> findAll(@Param("ids") List<Integer> messageIds); //find all messages not already in messageIds

    void clean(@Param("beatTime") int beatTime);
}
