/*
 * ExternalizableHelper
 *
 * Created 3/23/2007
 */
package com.topcoder.shared.netCommon;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;

/**
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public class ExternalizableHelper {
    public static final String DEFAULT_HANDLER_VALUE = "com.topcoder.server.serialization.ExternalizableCSHandler";
    public static final String DEFAULT_HANDLER_KEY = "com.topcoder.shared.netCommon.externalizable.handlerClass";

    private static Constructor handlerConstructor ;

    private static ThreadLocal threadLocal = new ThreadLocal() {
        protected Object initialValue() {
            try {
                if (handlerConstructor == null) {
                    String className = System.getProperty(DEFAULT_HANDLER_KEY, DEFAULT_HANDLER_VALUE);
                    handlerConstructor = Class.forName(className).getConstructor(new Class[]{});
                }
                return handlerConstructor.newInstance(new Object[]{});
            } catch (Exception e) {
                throw (IllegalStateException) new IllegalStateException("Exception trying to instantiate externalizable handler").initCause(e);
            }
        }
    };

    public static void writeExternal(ObjectOutput out, CustomSerializable obj) throws IOException {
         CSHandler handler = getInstance();
         handler.setDataOutput(out);
         obj.customWriteObject(handler);
     }

     public static void readExternal(ObjectInput in, CustomSerializable obj) throws IOException {
         CSHandler handler = getInstance();
         handler.setDataInput(in);
         obj.customReadObject(handler);
     }

     public static CSHandler getInstance() {
         return (CSHandler) threadLocal.get();
     }
}
