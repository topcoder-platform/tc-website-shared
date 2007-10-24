/*
 * CustomSerializableMessageMapper
 * 
 * Created Oct 3, 2007
 */
package com.topcoder.shared.messagebus.jms.mapper.custom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import com.topcoder.shared.messagebus.BusMessage;
import com.topcoder.shared.messagebus.jms.mapper.MessageMapper;
import com.topcoder.shared.netCommon.CSHandler;

/**
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public class CustomSerializableMessageMapper extends MessageMapper {
    private static final String METHOD_NAME = "CUSTOM";
    private CSHandler handler;

    protected Message newJMSMessage(BusMessage src, Session session) throws JMSException {
        return session.createBytesMessage();
    }
    
    protected void fillMessageBody(BusMessage src, Message message) throws JMSException, IOException {
        MessageMapper.setMessageSerializationMethod(message, METHOD_NAME);
        byte[] byteArray = buildByteArray(src.getMessageBody());
        ((BytesMessage) message).writeBytes(byteArray);
    }
    
    protected void fillMessageBody(Message src, BusMessage message) throws JMSException, IOException {
        BytesMessage srcByte = (BytesMessage) src;
        byte[] buf = new byte[(int)srcByte.getBodyLength()];
        int bytes = srcByte.readBytes(buf);
        message.setMessageBody(buildObject(buf, bytes));
    }

    private CSHandler getCSHandler() {
        if (handler == null) {
            handler = new CSHandler() {
                protected boolean writeObjectOverride(Object object) throws IOException {
                    return false;
                }
            };
        }
        return handler;
    }

    private byte[] buildByteArray(Object o) throws IOException {
        CSHandler handler = getCSHandler();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
        DataOutputStream dataOut = new DataOutputStream(bos);
        handler.setDataOutput(dataOut);
        handler.writeObject(o);
        dataOut.flush();
        dataOut.close();
        byte[] byteArray = bos.toByteArray();
        handler.setDataOutput(null);
        return byteArray;
    }
    
    private Object buildObject(byte[] buf, int bytes) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(buf, 0, bytes));
        CSHandler handler = getCSHandler();
        handler.setDataInput(dataInputStream);
        Object object = handler.readObject();
        handler.setDataInput(null);
        return object;
    }
}
