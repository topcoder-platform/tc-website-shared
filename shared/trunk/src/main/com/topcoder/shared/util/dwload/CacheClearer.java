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
import com.topcoder.shared.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author rfairfax
 */
public class CacheClearer {
    private static final Logger log = Logger.getLogger(CacheClearer.class);

    /**
     * Creates a new instance of CacheClearer
     */
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
            removelike(s, "/", o);

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

    private static Set getChildrenNames(String s, Object cache) throws IllegalAccessException, InvocationTargetException {
        Method[] methods = cache.getClass().getDeclaredMethods();
        for (Method m : methods) {
            log.debug("method " + m.getName() + " params " + m.getParameterTypes());
            if ("getChildrenNames".equals(m.getName()) &&
                    m.getParameterTypes().length == 1 &&
                    m.getParameterTypes()[0].equals(String.class)) {
                return (Set) m.invoke(cache, s);
            }
        }
        throw new RuntimeException("Couldn't find getChildrenNames(String) method");

    }

    private static void remove(String s, Object cache) throws IllegalAccessException, InvocationTargetException {
        Method[] methods = cache.getClass().getDeclaredMethods();
        for (Method m : methods) {
            if ("remove".equals(m.getName()) &&
                    m.getParameterTypes().length == 1 &&
                    m.getParameterTypes()[0].equals(String.class)) {
                m.invoke(cache, s);
                return;
            }
        }
        throw new RuntimeException("Couldn't find getChildrenNames(String) method");

    }


    private static void removelike(String key, String parent, Object cache) throws IllegalAccessException, InvocationTargetException {
        String kid;
        String fqn;
        for (Object child : getChildrenNames(parent, cache)) {
            kid = (String) child;
            fqn = parent + "/" + kid;
            if (kid.indexOf(key) >= 0) {
                remove(fqn, cache);
            } else {
                Set kids = getChildrenNames(fqn, cache);
                if (kids != null && !kids.isEmpty()) {
                    removelike(key, fqn, cache);
                }
            }
        }
    }

}



