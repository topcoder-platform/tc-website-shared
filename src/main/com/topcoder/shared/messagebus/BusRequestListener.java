/*
 * BusRequestListener
 * 
 * Created Oct 1, 2007
 */
package com.topcoder.shared.messagebus;


/**
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public interface BusRequestListener {
    public interface Handler {
        BusMessage handle(BusMessage message);
    }
    
    void setHandler(Handler l) throws BusException;
    void start() throws BusException;
    void stop();
}
