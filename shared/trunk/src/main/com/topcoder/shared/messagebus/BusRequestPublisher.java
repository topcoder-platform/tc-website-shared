/*
 * BusRequestPublisher
 * 
 * Created Oct 1, 2007
 */
package com.topcoder.shared.messagebus;

import java.util.concurrent.Future;

/**
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public interface BusRequestPublisher {
    Future<BusMessage> request(BusMessage message);
    void release();
}
