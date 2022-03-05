package com.junling.mq.admin.dao;

import com.junling.mq.admin.registry.RegistryGroup;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RegistryGroupDao {

    RegistryGroup find(@Param("registryKey") String registryKey);

    void save(@Param("registryGroup") RegistryGroup group);


    void update(@Param("registryGroup") RegistryGroup registryGroup);

    void cleanInactive();

    List<RegistryGroup> pageList(@Param("offset") int offset, @Param("pageSize") int pageSize);
}
