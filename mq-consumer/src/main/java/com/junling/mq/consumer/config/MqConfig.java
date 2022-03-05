package com.junling.mq.consumer.config;

import com.junling.mq.core.factory.SpringClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    @Value("${mq.admin.address}")
    private String adminAddress;

    @Bean
    public SpringClientFactory getSpringClientFactory(){
        SpringClientFactory springClientFactory = new SpringClientFactory(adminAddress);
        return springClientFactory;
    }

}
