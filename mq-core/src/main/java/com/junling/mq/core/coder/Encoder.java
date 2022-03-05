package com.junling.mq.core.coder;

import com.junling.mq.core.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Encoder extends MessageToByteEncoder {
    private Class<?> beanClazz;
    private Serializer serializer;

    public Encoder(Class<?> beanClazz, Serializer serializer) {
        this.beanClazz = beanClazz;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {

    }
}
