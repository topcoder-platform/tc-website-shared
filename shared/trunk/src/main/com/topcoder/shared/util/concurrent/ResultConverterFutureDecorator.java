package com.topcoder.shared.util.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public abstract class ResultConverterFutureDecorator<T, V> implements Future<V> {
    private Future<T> future;

    protected abstract V convertResult(T value) throws InterruptedException, ExecutionException;
    
    
    public ResultConverterFutureDecorator(Future<T> future) {
        this.future = future;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    public V get() throws InterruptedException, ExecutionException {
        T value = future.get();
        return convertResult(value);
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        T value = future.get(timeout, unit);
        return convertResult(value);
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }

    public boolean isDone() {
        return future.isDone();
    }
}