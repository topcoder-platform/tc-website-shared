/*
 * BusPublisher
 * 
 * Created Oct 1, 2007
 */
package com.topcoder.shared.messagebus;


/**
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public interface BusPublisher {
    void publish(BusMessage message) throws BusException;
    void close();
}
