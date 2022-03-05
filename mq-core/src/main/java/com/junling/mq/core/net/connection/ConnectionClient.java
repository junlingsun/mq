package com.junling.mq.core.net.connection;

import com.junling.mq.core.model.RpcRequest;

public interface ConnectionClient {

    void asynSend(RpcRequest rpcRequest);

    boolean isValid();

    void close();
}
