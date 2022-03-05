package com.junling.mq.core.net.client;

import com.junling.mq.core.net.connection.ConnectionClient;
import com.junling.mq.core.net.connection.NettyConnectionClient;
import com.junling.mq.core.rpc.factory.RpcInvokerFactory;
import com.junling.mq.core.model.RpcRequest;
import com.junling.mq.core.serializer.Serializer;

import java.util.concurrent.ConcurrentHashMap;

public class NettyClient implements NetClient{

    private static volatile ConcurrentHashMap<String, ConnectionClient> connectionMap = new ConcurrentHashMap<>();

    @Override
    public void asyncSend(String remoteAddress, RpcRequest rpcRequest, Serializer serializer, RpcInvokerFactory invokerFactory) {

       if (!connectionMap.containsKey(remoteAddress)) {
           ConnectionClient connectionClient = new NettyConnectionClient(remoteAddress, serializer, invokerFactory);
           connectionMap.put(remoteAddress, connectionClient);
       }
       ConnectionClient connectionClient = connectionMap.get(remoteAddress);

       if (!connectionClient.isValid()) {
           connectionClient.close();
       }
       connectionClient.asynSend(rpcRequest);
    }
}
