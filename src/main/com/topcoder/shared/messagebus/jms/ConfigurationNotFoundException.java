/*
 * ConfigurationNotFoundException
 * 
 * Created Oct 6, 2007
 */
package com.topcoder.shared.messagebus.jms;

/**
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public class ConfigurationNotFoundException extends Exception {

    /**
     * 
     */
    public ConfigurationNotFoundException() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public ConfigurationNotFoundException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public ConfigurationNotFoundException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public ConfigurationNotFoundException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
