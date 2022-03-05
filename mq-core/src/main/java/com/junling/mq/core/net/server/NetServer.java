package com.junling.mq.core.net.server;

import java.util.Map;

public interface NetServer {
    void start(String remoteHost, int remotePort, Map<String, Object> serviceParamMap);
}
