package com.junling.mq.core.net.client;

import com.junling.mq.core.rpc.factory.RpcInvokerFactory;
import com.junling.mq.core.model.RpcRequest;
import com.junling.mq.core.serializer.Serializer;

public interface NetClient {
    void asyncSend(String remoteAddress, RpcRequest rpcRequest, Serializer serializer, RpcInvokerFactory invokerFactory);
}
