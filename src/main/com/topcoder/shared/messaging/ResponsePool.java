package com.topcoder.shared.messaging;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

/**
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
        dropList.add(correlationId);
        throw new TimeOutException();
    }


    /**
     *
     * @param key
     * @param val
     */
    public synchronized void put(Object key, Object val) {
        responseMap.put(key, val);
        notifyAll();
    }

    /**
     *
     * @param correlationId
     * @return
     */
    public synchronized boolean isOldResponse(String correlationId) {
        return dropList.contains(correlationId);
    }

    /**
     *
     * @param correlationId
     */
    public synchronized void removeDroppedResponse(String correlationId) {
        dropList.remove(correlationId);
    }



}
