package com.junling.mq.core.model;

import com.junling.mq.core.rpc.factory.RpcInvokerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureRpcResponse implements Future {

    private RpcResponse rpcResponse;
    private String uuid;
    private RpcInvokerFactory invokerFactory;

    private Object lock = new Object();
    private boolean done = false;

    public FutureRpcResponse(String uuid, RpcInvokerFactory invokerFactory) {
        this.uuid= uuid;
        this.invokerFactory =invokerFactory;
        invokerFactory.setFuture(uuid, this);
    }


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        try {
            return get(-1, TimeUnit.SECONDS);
        }
        catch (TimeoutException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!done) {
            synchronized (lock) {
                if (timeout < 0) {
                    lock.wait();
                }else {
                    long sleep = TimeUnit.MICROSECONDS.convert(timeout, unit);
                    lock.wait(sleep);
                }
            }
        }

        if (!done) {
            new RuntimeException("response times out");
        }


        return rpcResponse;
    }

    public void setResponse(RpcResponse rpcResponse) {

        synchronized (lock) {
            done = true;
            this.rpcResponse = rpcResponse;
            lock.notifyAll();
        }
    }
}
