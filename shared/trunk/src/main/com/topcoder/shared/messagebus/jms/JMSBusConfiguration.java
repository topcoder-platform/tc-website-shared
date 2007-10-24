/*
 * JMSBusConfiguration
 * 
 * Created Oct 6, 2007
 */
package com.topcoder.shared.messagebus.jms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.topcoder.shared.messagebus.jms.mapper.MessageMapperConfiguration;

/**
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public class JMSBusConfiguration {
    private Map<String, JMSChannelConfiguration> channels = new HashMap<String, JMSChannelConfiguration>();
    private Map<String, JMSConnectorConfiguration> connectors = new HashMap<String, JMSConnectorConfiguration>();
    private List<MessageMapperConfiguration> mappers = new ArrayList<MessageMapperConfiguration>();
    
    
    public void addChannel(JMSChannelConfiguration channel) {
        if (channel.getExtendsConfig() != null) {
            channel.fillFrom(channels.get(channel.getExtendsConfig()));
        }
        channels.put(channel.getName(), channel);
    }
    
    public void addConnector(JMSConnectorConfiguration connector) {
        connectors.put(buildKey(connector.getType(), connector.getKey(), connector.getModule()), connector);
    }
    
    private String buildKey(String type, String key, String module) {
        if (module != null && module.trim().length() == 0) {
            module = null;
        }
        StringBuilder sb = new StringBuilder(type.length()+key.length()+ (module!=null ? module.length() : 0) +12);
        sb.append(type).append("||")
          .append(key).append("||")
          .append(module);
        return sb.toString();
    }

    public void addMapper(MessageMapperConfiguration mapper) {
        mappers.add(mapper);
    }
    
    public Map<String, JMSChannelConfiguration> getChannels() {
        return channels;
    }

    public Map<String, JMSConnectorConfiguration> getConnectors() {
        return connectors;
    }

    public List<MessageMapperConfiguration> getMappers() {
        return mappers;
    }

    public JMSChannelConfiguration getChannelForConnector(String type, String configurationKey, String moduleName) throws ConfigurationNotFoundException {
        JMSConnectorConfiguration connector = getConnectorConfiguration(type, configurationKey, moduleName);
        if (connector == null) {
            connector = getConnectorConfiguration(type, configurationKey, null);
            if (connector == null) {
                throw new ConfigurationNotFoundException("Could not find connector configuration: type="+type+" configurationKey="+configurationKey+" moduleName="+moduleName);
            }
        }
        String channelName = connector.getChannel();
        return getChannel(channelName);
    }

    private JMSChannelConfiguration getChannel(String channelName) throws ConfigurationNotFoundException {
        JMSChannelConfiguration channelConfiguration = channels.get(channelName);
        if (channelConfiguration == null) {
            throw new ConfigurationNotFoundException("Could not find channel configuration: channel="+channelName);
        }
        return channelConfiguration;
    }

    private JMSConnectorConfiguration getConnectorConfiguration(String type, String configurationKey, String moduleName) {
        return connectors.get(buildKey(type, configurationKey, moduleName));
    }
}
