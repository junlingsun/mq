package com.junling.mq.core.net.connection;

import com.junling.mq.core.coder.Decoder;
import com.junling.mq.core.coder.Encoder;
import com.junling.mq.core.rpc.factory.RpcInvokerFactory;
import com.junling.mq.core.model.RpcRequest;
import com.junling.mq.core.model.RpcResponse;
import com.junling.mq.core.serializer.Serializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;

public class NettyConnectionClient implements ConnectionClient{

    private String remoteAddress;
    private EventLoopGroup group;
    private Channel channel;


    public NettyConnectionClient(String remoteAddress, Serializer serializer, RpcInvokerFactory invokerFactory) {
        this.remoteAddress = remoteAddress;
        String[] strs = remoteAddress.split(":");
        String host = strs[0];
        int port = Integer.parseInt(strs[1]);

        group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline()
                                .addLast(new Encoder(RpcRequest.class, serializer))
                                .addLast(new Decoder(RpcResponse.class, serializer))
                                .addLast(new SimpleChannelInboundHandler<RpcResponse>() {

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
                                        String uuid = rpcResponse.getUuid();
                                        invokerFactory.notify(uuid, rpcResponse);

                                    }
                                });
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        try {
            this.channel = bootstrap.connect(host, port).sync().channel();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void asynSend(RpcRequest rpcRequest) {
        channel.writeAndFlush(rpcRequest);

    }

    @Override
    public boolean isValid() {

        if (channel != null) {
            return channel.isActive();
        }
        return false;
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.close();
        }
    }
}
