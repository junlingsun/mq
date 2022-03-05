package com.junling.mq.core.net;

import com.junling.mq.core.net.client.NetClient;
import com.junling.mq.core.net.server.NetServer;
import com.junling.mq.core.net.server.NettyServer;

public enum NetType {
    netty(NettyServer.class, NetClient.class); //TODO: add other servers

    private Class<? extends NetServer> netServerClazz;
    private Class<? extends NetClient> netClientClazz;

    NetType(Class<? extends NetServer> netServerClazz, Class<? extends NetClient> netClientClazz) {
        this.netServerClazz = netServerClazz;
        this.netClientClazz = netClientClazz;
    }

    public void setNetServerClazz(Class<? extends NetServer> netServerClazz) {
        this.netServerClazz = netServerClazz;
    }

    public void setNetClientClazz(Class<? extends NetClient> netClientClazz) {
        this.netClientClazz = netClientClazz;
    }

    public Class<? extends NetServer> getNetServerClazz() {
        return netServerClazz;
    }

    public Class<? extends NetClient> getNetClientClazz() {
        return netClientClazz;
    }
}
