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
    protected HashSet dropList = new HashSet();
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
     * @param timeoutLength
     * @param correlationId
     * @return
     * @throws TimeOutException
     */
    public synchronized Serializable get(int timeoutLength, String correlationId) throws TimeOutException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeoutLength;
        while (System.currentTimeMillis() < endTime) {
            if (responseMap.containsKey(correlationId)) {
                return (Serializable) responseMap.get(correlationId);
            } else {
                try {
                    wait(waitTime);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
        //perhaps a change is in order.  if this got very large, we could have a
        //memory problem.  perhaps we could track the the people waiting
        //and throw out stuff that doesn't below to someone waiting.
        dropList.add(correlationId);
        throw new TimeOutException();
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
     * up and dies
     * @param correlationId
     * @return
     */
    public synchronized boolean isOldResponse(String correlationId) {
        return dropList.contains(correlationId);
    }

    /**
     * Remove an orphaned response.
     * @param correlationId
     */
    public synchronized void removeDroppedResponse(String correlationId) {
        dropList.remove(correlationId);
    }



}
