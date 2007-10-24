/*
 * RoundEventPublisher
 * 
 * Created 09/28/2007
 */
package com.topcoder.shared.round.events;

/**
 * RoundEventPublisher interface.
 * 
 * Implementors of this interface are responsible for publishing
 * Round Events.
 * 
 * Users of this class must release the publisher after finishing its work.
 * 
 * RoundEventPublisher are not synchronized,external synchronization should be used or
 * different instances should be created.
 * 
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public interface RoundEventPublisher {
    
    /**
     * Publish a RoundEvent
     * @param event The event to publish
     * 
     * @throws RoundEventException If the event could not be published
     */
    void publishEvent(RoundEvent event) throws RoundEventException;
    
    /**
     * Releases this publisher.<p>
     * After releasing fireEvent must not be called anymore.
     */
    void release();
}
