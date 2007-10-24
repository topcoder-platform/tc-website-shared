/*
 * BusFactory
 * 
 * Created 10/01/2007
 */
package com.topcoder.shared.messagebus;



/**
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public abstract class BusFactory {
    private static BusFactory factory = null;
    
    public static BusFactory getFactory() {
        if (factory == null) {
            throw new IllegalStateException("The Bus factory is not configured");
        }
        return factory;
    }
    
    public static void configureFactory(BusFactory factory) {
        BusFactory.factory = factory;
    }

    public abstract BusPublisher createPublisher(String configurationKey, String moduleName) throws BusFactoryException;
    
    public abstract BusListener createListener(String configurationKey, String moduleName) throws BusFactoryException;
    
    public abstract BusPollListener createPollListener(String configurationKey, String moduleName) throws BusFactoryException;

    public abstract BusRequestPublisher createRequestPublisher(String configurationKey, String moduleName) throws BusFactoryException;
    
    public abstract BusRequestListener createRequestListener(String configurationKey, String moduleName) throws BusFactoryException;
    
    public abstract void release();
}
