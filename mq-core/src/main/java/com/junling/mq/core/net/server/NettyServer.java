package com.junling.mq.core.net.server;

import com.junling.mq.core.coder.Decoder;
import com.junling.mq.core.coder.Encoder;
import com.junling.mq.core.model.RpcRequest;
import com.junling.mq.core.model.RpcResponse;
import com.junling.mq.core.serializer.impl.HessianSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NettyServer implements NetServer{

    @Override
    public void start(String remoteHost, int remotePort, Map<String, Object> serviceParamMap) {
        Thread thread = new Thread(()->{
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(30, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try{
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(Channel channel) throws Exception {
                                channel.pipeline()
                                        .addLast(new Decoder(RpcRequest.class, new HessianSerializer()))
                                        .addLast(new Encoder(RpcResponse.class, new HessianSerializer()))
                                        .addLast(new SimpleChannelInboundHandler<RpcRequest>() {
                                            @Override
                                            protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
                                                threadPoolExecutor.execute(()->{
                                                    try {
                                                        RpcResponse rpcResponse = invoke(ctx, rpcRequest, serviceParamMap);
                                                        ctx.writeAndFlush(rpcResponse);
                                                    }
                                                    catch (NoSuchMethodException e) {
                                                        e.printStackTrace();
                                                    }
                                                    catch (IllegalAccessException e) {
                                                        e.printStackTrace();
                                                    }
                                                    catch (InvocationTargetException e) {
                                                        e.printStackTrace();
                                                    }
                                                });



                                            }
                                        });
                            }
                        })
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                try {
                    ChannelFuture channelFuture = bootstrap.bind(remotePort).sync();
                    channelFuture.channel().closeFuture().sync();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }finally {
                threadPoolExecutor.shutdown();
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }



        });

        thread.setDaemon(true);
        thread.start();
    }

    private RpcResponse invoke(ChannelHandlerContext ctx, RpcRequest rpcRequest, Map<String, Object> serviceParamMap) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String className = rpcRequest.getClassName();
        String methodName = rpcRequest.getMethodName();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();

        RpcResponse rpcResponse = new RpcResponse();

        Object serviceBean = serviceParamMap.get(className);
        if (serviceBean == null) {
            rpcResponse.setCode(500);
            rpcResponse.setMsg("service " + className + " was not found");
            return rpcResponse;
        }

        Method method = serviceBean.getClass().getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        Object invoke = method.invoke(serviceBean, parameters);

        rpcResponse.setUuid(rpcRequest.getUuid());
        rpcResponse.setCode(200);
        rpcResponse.setMsg("SUCCESS");
        rpcResponse.setData(invoke);

        return rpcResponse;
    }
}
