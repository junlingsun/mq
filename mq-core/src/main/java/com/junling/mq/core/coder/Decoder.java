package com.junling.mq.core.coder;

import com.junling.mq.core.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class Decoder extends ByteToMessageDecoder {

    private Class<?> beanClazz;
    private Serializer serializer;

    public Decoder(Class<?> beanClazz, Serializer serializer) {
        this.beanClazz = beanClazz;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

    }
}
