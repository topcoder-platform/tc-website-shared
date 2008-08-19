/*
 * ServiceEventListener
 * 
 * Created 04/25/2006
 */
package com.topcoder.shared.serviceevent;

import javax.jms.ObjectMessage;

import com.topcoder.shared.common.ApplicationServer;
import com.topcoder.shared.messaging.TopicMessageSubscriber;
import com.topcoder.shared.util.logging.Logger;

/**
 * 
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public class ServiceEventListener implements Runnable {

    private final Logger logger = Logger.getLogger(ServiceEventListener.class);
    
    /**
     * Events are received through this TopicMessageSubscriber
     */
    private TopicMessageSubscriber tms;

    private final static int TIMEOUT = 30000;
    private final static int MAX_ERRORS = 3;
    
    private ServiceEventMessageListener realListener;

    private Thread thread;

    private volatile boolean stopped;

    /**
    * Constructs a new ServiceEventListener.
    * 
    * @throws IllegalStateException If a problem arises when triying to start the
    *                              listener mechanism
    */
    public ServiceEventListener(String topicName, String serviceName) throws IllegalStateException {
        try {
            realListener = new ServiceEventMessageListener(serviceName);
            logger.info("Initializing ServiceEventListener for topic="+topicName+" service="+serviceName);
            tms = new TopicMessageSubscriber(ApplicationServer.JMS_FACTORY, topicName);
            tms.setSelector("serviceName = '" + serviceName + "'");
            tms.setFaultTolerant(false);
            thread = new Thread(this, "SvcEventListener["+topicName+","+serviceName+"]");
            thread.setDaemon(true);
            thread.start();
            logger.info("ServiceEventListener connected");
        } catch (Exception e) {
            logger.error("Error initializing ServiceEventListener.", e);
            throw new IllegalStateException("Error initializing ServiceEventListener");
        }
    }
    
    public void release() {
        try {
            if (thread != null) {
                stopped = true;
                thread.interrupt();
                realListener.clearListeners();
                tms.close();
                thread = null;
                tms = null;
            }
        } catch (RuntimeException e) {
            logger.error("Exception releasing Listener", e);
        }
    }
    
    public void addListener(String eventType, ServiceEventHandler listener) {
        realListener.addListener(eventType, listener);
    }
    
    public void removeListener(ServiceEventHandler listener) {
        realListener.removeListener(listener);
    }
    
    
   /* Listens for messages on the topic.  When a message comes in, the method
    * calls notifyListener 
    */
   public void run() {
        int errorCount = 0;
        while (errorCount < MAX_ERRORS && !stopped) {
            ObjectMessage message = null;
            try {
                logger.debug("Trying to get message.");
                message = tms.getMessage(TIMEOUT);
                if (logger.isDebugEnabled()) {
                    logger.debug("Got message: " + message);
                }
            } catch (Exception e) {
                errorCount++;
                logger.error("Error reading message from topic.", e);
            }

            realListener.onMessage(message);
        }
        logger.error("Too many errors in ServiceEventListener, giving up connections.");
    }
}
