/*
 * NullFuture
 * 
 * Created Nov 2, 2007
 */
package com.topcoder.shared.util.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public class NullFuture<V> implements Future<V>{

    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    public V get() throws InterruptedException, ExecutionException {
        return null;
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    public boolean isCancelled() {
        return false;
    }

    public boolean isDone() {
        return true;
    }
}
