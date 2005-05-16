package com.topcoder.shared.messaging;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
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
    protected Map responseMap = new HashMap();
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
            if (responseMap.containsKey(correlationId)) {
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
        return (Serializable)responseMap.remove(correlationId);

    }


    /**
     * Put an object into the pool
     * @param key
     * @param val
     */
    public synchronized void put(Object key, Object val) {
        responseMap.put(key, val);
        notifyAll();
    }

    /**
     * Check if a response has been orphaned.  A response
     * will be orphaned if it took too long for it to show
     * up and the client is no longer waiting for it
     * @param correlationId
     * @return
     */
    public synchronized boolean isOldResponse(String correlationId) {
        return !waitList.contains(correlationId);
    }

    /**
     * Remove an orphaned response.
     * @param correlationId
     */
    public synchronized void removeDroppedResponse(String correlationId) {
        waitList.remove(correlationId);
        responseMap.remove(correlationId);
    }




}
