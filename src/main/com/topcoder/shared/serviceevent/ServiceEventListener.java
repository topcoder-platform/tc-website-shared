/*
 * ServiceEventListener
 * 
 * Created 04/25/2006
 */
package com.topcoder.shared.serviceevent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * Map containing listeners for eventType
     */
    private Map listeners = new HashMap();

    private Thread thread;

    private volatile boolean stopped;

    private String serviceName;
    
    /**
    * Constructs a new ServiceEventListener.
    * 
    * @throws IllegalStateException If a problem arises when triying to start the
    *                              listener mechanism
    */
    public ServiceEventListener(String topicName, String serviceName) throws IllegalStateException {
        try {
            this.serviceName = serviceName;
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
                listeners.clear();
                tms.close();
                thread = null;
                tms = null;
            }
        } catch (RuntimeException e) {
            logger.error("Exception releasing Listener", e);
        }
    }
    
    public void addListener(String eventType, Listener listener) {
        List typeListeners = null;
        synchronized (listeners) {
            typeListeners = (List) listeners.get(eventType);
            if (typeListeners == null) {
                typeListeners = new ArrayList();
                listeners.put(eventType, typeListeners);
            }
            typeListeners.add(listener);
        }
    }
    
    public void removeListener(Listener listener) {
        synchronized (listeners) {
            for (Iterator it = listeners.values().iterator(); it.hasNext();) {
                Set typeListeners= (Set) it.next();
                typeListeners.remove(listener);
            }
            listeners.remove(listener);
        }
    }
    
    protected void notifyListeners(String eventType, Serializable eventObject) {
        List allListeners = null;
        synchronized (listeners) {
            List typeListeners = (List) listeners.get(eventType);
            if (typeListeners == null) {
                allListeners = new ArrayList();
            } else {
                allListeners = new ArrayList(typeListeners);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Incoming notification: "+serviceName+"#"+eventType);
        }
        for (Iterator it = allListeners.iterator(); it.hasNext();) {
            Listener listener = (Listener) it.next();
            try {
                listener.eventReceived(eventType, eventObject);
            } catch (RuntimeException e) {
                logger.error("ServiceEventListener.Listener throw exception during notification.", e);
            }
        }
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

            if (message != null) {
                try {
                    if (message.propertyExists("eventType")) {
                        notifyListeners(message.getStringProperty("eventType"), message.getObject());
                    }
                } catch (Exception e) {
                    logger.error("Invalid message received.", e);
                }
            }
        }
        logger.error("Too many errors in ServiceEventListener, giving up connections.");
    }    
    
    public interface Listener {
        public void eventReceived(String eventType, Serializable object);
    }
}
