package com.junling.mq.core.serializer;

import com.junling.mq.core.serializer.impl.HessianSerializer;

public enum SerializerType {
    hessian(HessianSerializer.class); //TODO: add more serializers

    private Class<? extends Serializer> serializerClazz;

    SerializerType(Class<? extends Serializer> serializerClazz) {
        this.serializerClazz = serializerClazz;
    }

    public void setSerializerClazz(Class<? extends Serializer> serializerClazz) {
        this.serializerClazz = serializerClazz;
    }

    public Class<? extends Serializer> getSerializerClazz() {
        return serializerClazz;
    }
}
