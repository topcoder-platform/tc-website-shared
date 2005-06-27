package com.topcoder.shared.util;

import com.topcoder.shared.util.logging.Logger;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author unknown
 * @version  $Revision$
 */
public final class TCResourceBundle {

    private static Logger log = Logger.getLogger(TCResourceBundle.class);

    private ResourceBundle bundle;

    /**
     *
     * @param baseName
     */
    public TCResourceBundle(String baseName) {
        try {
            bundle = ResourceBundle.getBundle(baseName);
        } catch (MissingResourceException e) {
            error("", e);
        }
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getProperty(String key, String defaultValue) {
        String ret = null;
        try {
            ret = getProperty(key);
            //log.debug("setting " + key + " = " + ret);
        } catch (MissingResourceException e) {
            ret = defaultValue;
            log.debug("key not found, setting default " + key + " = " + ret);
        }
        return ret;
    }
 
    /**
     * 
     * @param key 
     * @param defaultValue 
     * @return 
     */
    public int getIntProperty(String key, int defaultValue) {
        String str = getProperty(key, Integer.toString(defaultValue));
        return Integer.parseInt(str.trim());
    }

    /**
     * Type-safe getter for double properties, with default value.
     *
     * @param key The property's key.
     * @param defaultValue Value to use if the key isn't found.
     * @return The property's value as a double.
     */
    public double getDoubleProperty(String key, double defaultValue) {
        String str = getProperty(key, Double.toString(defaultValue));
        return Double.parseDouble(str.trim());
    }

    /**
     *
     * @param key
     * @return
     * @throws MissingResourceException
     */
    public String getProperty(String key) throws MissingResourceException {
        return bundle.getString(key);
    }

    /**
     * 
     * @param key
     * @return
     * @throws MissingResourceException
     */
    public int getIntProperty(String key) throws MissingResourceException {
        String str = getProperty(key);
        return Integer.parseInt(str.trim());
    }

    /**
     * Type-safe getter for double properties.
     * 
     * @param key The property's key.
     * @return The property's value as a double.
     * @throws java.util.MissingResourceException If the key isn't found.
     */
    public double getDoubleProperty(String key) throws MissingResourceException {
        String str = getProperty(key);
        return Double.parseDouble(str.trim());
    }
    
    /**
     *
     * @param message
     * @param t
     */
    private static void error(Object message, Throwable t) {
        log.error(message, t);
    }
}
