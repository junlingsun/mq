package com.junling.mq.core.model;

public class RpcResponse<T> {

    private String uuid;
    private Integer code;
    private String msg;
    private T data;

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getUuid() {
        return uuid;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
