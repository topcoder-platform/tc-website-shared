/*
 * JMSChannelConfiguration
 * 
 * Created Oct 3, 2007
 */
package com.topcoder.shared.messagebus.jms;

import java.util.HashMap;


/**
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public class JMSChannelConfiguration {
    public static final String TOPIC = "TOPIC";
    public static final String QUEUE = "QUEUE";
    
    private String name;
    private String extendsConfig;
    private String destinationType;
    private String destinationName;
    private HashMap<String, String> properties = new HashMap<String, String>();
    private Boolean sharedConnection;
    private String sharedConnectionName;
    private String selectorString;
    private Boolean noLocal;
    private Boolean durableSubscriber;
    private String  durableSubscriberName;
    
    public String getDestinationType() {
        return destinationType;
    }
    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }
    public String getDestinationName() {
        return destinationName;
    }
    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }
    public boolean isSharedConnection() {
        return sharedConnection.booleanValue();
    }
    public void setSharedConnection(boolean sharedConnection) {
        this.sharedConnection = Boolean.valueOf(sharedConnection);
    }
    public String getSharedConnectionName() {
        return sharedConnectionName;
    }
    public void setSharedConnectionName(String sharedConnectionName) {
        this.sharedConnectionName = sharedConnectionName;
    }
    public String getSelectorString() {
        return selectorString;
    }
    public void setSelectorString(String selectorString) {
        this.selectorString = selectorString;
    }
    public boolean isNoLocal() {
        return noLocal.booleanValue();
    }
    public void setNoLocal(boolean noLocal) {
        this.noLocal = Boolean.valueOf(noLocal);
    }
    public boolean isDurableSubscriber() {
        return durableSubscriber;
    }
    public void setDurableSubscriber(boolean durableSubscriber) {
        this.durableSubscriber = durableSubscriber;
    }
    public String getDurableSubscriberName() {
        return durableSubscriberName;
    }
    public void setDurableSubscriberName(String durableSubscriberName) {
        this.durableSubscriberName = durableSubscriberName;
    }
    public String getExtendsConfig() {
        return extendsConfig;
    }
    public void setExtendsConfig(String extendsConfig) {
        this.extendsConfig = extendsConfig;
    }
    public void fillFrom(JMSChannelConfiguration src) {
        if (this.destinationType == null) this.destinationType = src.destinationType;
        if (this.destinationName == null) this.destinationName = src.destinationName;
        if (this.sharedConnection == null) this.sharedConnection = src.sharedConnection;
        if (this.sharedConnectionName == null) this.sharedConnectionName = src.sharedConnectionName;
        if (this.selectorString == null) this.selectorString = src.selectorString;
        if (this.noLocal == null) this.noLocal = src.noLocal;
        if (this.durableSubscriber == null) this.durableSubscriber = src.durableSubscriber;
        if (this.durableSubscriberName == null) this.durableSubscriberName = src.durableSubscriberName;
        HashMap<String, String> customProps = new HashMap<String, String>(src.properties);
        customProps.putAll(this.properties);
        this.properties = customProps;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public HashMap<String, String> getProperties() {
        return properties;
    }
    
    public void addProperty(Property property) {
        properties.put(property.getName(), property.getValue());
    }
    
    public static class Property {
        private String name;
        private String value;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }
}
