package com.topcoder.shared.messaging;

import com.topcoder.shared.distCache.Cache;
import com.topcoder.shared.distCache.CachedValue;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Provides a way to asynchronously receive messages from a queue including the ability
 * to wait for a specified period of time only, and then quit.
 * User: dok
 * Date: Dec 15, 2004
 * Time: 4:09:48 PM
 */
public class ResponsePool {

    private int DEFAULT_WAIT_TIME = 10;
    /**
     * Contains a map of the responses that we have gotten off the queue
     */
    protected Cache pool = new Cache();
    /**
     * List of correlation id's for responses that someone is actually waiting for
     */
    protected HashSet waitList = new HashSet();
    protected int waitTime;

    public ResponsePool() {
        this.waitTime = DEFAULT_WAIT_TIME;
    }

    public ResponsePool(int waitTime) {
        this.waitTime = waitTime;
    }

    /**
     * Polls the response pool for <code>timeoutLength</code> milliseconds for
     * a message associated with the <cocde>correlationId</code>
     *
     * If it is found, the response is removed from the pool and returned.
     * @param timeoutLength
     * @param correlationId
     * @return
     * @throws TimeOutException
     */
    public synchronized Serializable get(int timeoutLength, String correlationId) throws TimeOutException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeoutLength;
        waitList.add(correlationId);
        while (System.currentTimeMillis() < endTime) {
            if (pool.exists(correlationId)) {
                return get(correlationId);
            } else {
                try {
                    wait(waitTime);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
        //get it and ignore it cuz we don't care
        get(correlationId);
        throw new TimeOutException();
    }

    /**
     * Return the value for the specified key from this pool.
     * This call removes the specified entry in the pool as well
     * as from the list of responses that are being waited for
     * @param correlationId
     * @return
     */
    protected synchronized Serializable get(String correlationId) {
        waitList.remove(correlationId);
        CachedValue s = pool.remove(correlationId);
        if (s==null) {
            return null;
        } else {
            return (Serializable)s.getValue();
        }
    }


    /**
     * Put an object into the pool.  The object will automatically be removed after
     * a while if it is never requested.
     * @param key
     * @param val
     */
    public synchronized void put(String key, Object val) {
        //have stuff time out after 5 minutes.
        pool.update(key, val, 5*60*1000);
        notifyAll();
    }

}
