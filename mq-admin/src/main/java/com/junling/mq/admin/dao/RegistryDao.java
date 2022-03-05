package com.junling.mq.admin.dao;

import com.junling.mq.admin.registry.RegistryData;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RegistryDao {

    int update(@Param("registryData") RegistryData registryData);

    void save(@Param("registryData") RegistryData registryData);

    List<RegistryData> findAll(@Param("registryKey") String registryKey);

    void clean(@Param("timeout") int timeout);
}
