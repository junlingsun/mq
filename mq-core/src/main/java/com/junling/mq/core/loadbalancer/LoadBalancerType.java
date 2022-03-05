package com.junling.mq.core.loadbalancer;

import com.junling.mq.core.loadbalancer.impl.RandomLB;
import com.junling.mq.core.loadbalancer.impl.RoundLB;

public enum LoadBalancerType {
    random(RandomLB.class),
    round(RoundLB.class);

    private Class<? extends LoadBalancer> loadBalancerClazz;

    LoadBalancerType(Class<? extends LoadBalancer> loadBalancerClazz) {
        this.loadBalancerClazz = loadBalancerClazz;
    }

    public void setLoadBalancerClazz(Class<? extends LoadBalancer> loadBalancerClazz) {
        this.loadBalancerClazz = loadBalancerClazz;
    }

    public Class<? extends LoadBalancer> getLoadBalancerClazz() {
        return loadBalancerClazz;
    }
}
