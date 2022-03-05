package com.junling.mq.admin.config;

import com.junling.mq.admin.broker.Broker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrokerConfig {

    @Value("${remote.host}")
    private String remoteHost;

    @Value("${remote.port}")
    private int remotePort;


    @Bean
    public Broker getBroker(){
        Broker broker = new Broker(remoteHost, remotePort);
        return broker;

    }
}
