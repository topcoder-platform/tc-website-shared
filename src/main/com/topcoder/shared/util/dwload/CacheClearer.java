/*
 * CacheClearer.java
 *
 * Created on September 13, 2005, 11:33 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.topcoder.shared.util.dwload;

import com.topcoder.shared.util.TCContext;
import com.topcoder.shared.util.TCResourceBundle;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 *
 * @author rfairfax
 */
public class CacheClearer {
    
    /** Creates a new instance of CacheClearer */
    public CacheClearer() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        TCLoadTCS load = new TCLoadTCS();
        try {
            load.doClearCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removelike(String s) {
        InitialContext ctx = null;
        TCResourceBundle b = new TCResourceBundle("cache");
        try {
            ctx = TCContext.getInitial(b.getProperty("host_url"));
            //using reflection so that we don't a lot of nasty dependencies when using the class.
            Object o = ctx.lookup(b.getProperty("jndi_name"));
            Method[] methods = o.getClass().getDeclaredMethods();
            Set keys = null;
            for (Method m : methods) {
                if ("getKeys".equals(m.getName())) {
                    for (Class c : m.getParameterTypes()) {
                        if ("java.lang.String".equals(c.getName())) {
                            keys = (Set)m.invoke(o, "/");
                        }
                    }
                }
            }

            Method myMethod = null;
            for (Method m : methods) {
                if ("remove".equals(m.getName())) {
                    Class[] classes =m.getParameterTypes();
                    if (classes.length==2 && "java.lang.String".equals(classes[0].getName())
                            && "java.lang.Object".equals(classes[1].getName())) {
                        myMethod = m;
                    }
                }
            }
            String mykey;
            for (Object key : keys) {
                if (((String)key).indexOf(s)>=0) {
                    myMethod.invoke(o, "/", key);
                }
            }


        } catch (NamingException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            TCContext.close(ctx);
        }

    }

    
}
