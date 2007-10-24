/*
 * MapperProviderException
 * 
 * Created Oct 5, 2007
 */
package com.topcoder.shared.messagebus.jms.mapper;

/**
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public class MapperProviderException extends Exception {

    /**
     * 
     */
    public MapperProviderException() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public MapperProviderException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public MapperProviderException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public MapperProviderException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
