/*
 * Message
 * 
 * Created 07/18/2007
 */
package com.topcoder.shared.i18n;

import java.io.Serializable;

/**
 * The Message class represents a self contained message.
 * 
 * A message contains the required information that allows 
 * rendering the message in different languages/formats.
 * 
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public class Message implements Serializable {
    /**
     * The bundle name where they key must be searched
     */
    private String bundleName;
    /**
     * The key of the message
     */
    private String key;
    /**
     * Value arguments that can be merged into the resulting message 
     */
    private Object[] values;
    
    
    public Message(String bundleName, String key) {
        this.bundleName = bundleName;
        this.key = key;
    }

    public Message(String bundleName, String key, Object[] values) {
        this.bundleName = bundleName;
        this.key = key;
        this.values = values;
    }

    public String getKey() {
        return key;
    }

    public Object[] getValues() {
        return values;
    }

    public String getBundleName() {
        return bundleName;
    }
}
