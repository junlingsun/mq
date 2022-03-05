package com.junling.mq.core.factory;

import com.junling.mq.core.annotation.ConsumerBean;
import com.junling.mq.core.consumer.IConsumer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpringClientFactory implements ApplicationContextAware {
    private String adminAddress;
    private ClientFactory clientFactory;

    public SpringClientFactory(String adminAddress) {
        this.adminAddress = adminAddress;
        clientFactory = new ClientFactory(adminAddress);
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        List<IConsumer> consumerList = new ArrayList<>();
        Map<String, Object> beanMap = ac.getBeansWithAnnotation(ConsumerBean.class);

        if (beanMap != null && beanMap.size() > 0) {
            for (Object bean: beanMap.values()) {
                if (bean instanceof IConsumer) {
                    consumerList.add((IConsumer) bean);
                }
            }
        }

        clientFactory.setConsumers(consumerList);
        clientFactory.start();
    }
}
