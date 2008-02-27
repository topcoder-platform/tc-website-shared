package com.topcoder.shared.ejb;

import com.topcoder.shared.util.TCContext;
import com.topcoder.shared.util.logging.Logger;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;

/**
 * @author dok
 * @version $Id$
 *          Create Date: Feb 27, 2008
 */
public abstract class EJB3Locator<T> {

    private final Logger log = Logger.getLogger(EJB3Locator.class);

    /**
     * The jndi name where the bean can be found for local calls
     */
    private final String localJNDIName;

    /**
     * The jndi name where the bean can be found for remote calls
     */
    private final String remoteJNDIName;

    /**
     * The proxy returned to all getService calls.
     */
    private final T proxiedServices;

    /**
     * Indicates if a new instance of the service must be obtained.
     */
    private volatile boolean mustReload = true;

    /**
     * The real service where to delegate calls.
     */
    private T services;

    /**
     * The initial context URL
     */
    private String contextURL;

    /**
     * whether or not we should attempt to get the local bean before looking for the remote one
     */
    private boolean tryLocalFirst = false;

    private String iName;

    /**
     * Creates a new Service locator.
     * <p/>
     * <p/>
     * <p/>
     * With this constructor, the jndiname is defaulted to use the EJB3 default.
     * interface+Bean+/local and interface+Bean+/remote
     * <p/>
     * So, if you pass in com.topcoder.myinterface, you'll be looking up myinterfaceBean/local and myinterfaceBean/remote
     *
     * @param contextURL    The initial context URL.
     * @param tryLocalFirst whether or not we should attempt to get the local bean before looking for the remote one
     */
    public EJB3Locator(String contextURL, boolean tryLocalFirst) {
        Class<T> clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        iName = clazz.getName().substring(clazz.getName().lastIndexOf('.') + 1);
        this.localJNDIName = iName + "Bean/local";
        this.remoteJNDIName = iName + "Bean/remote";
        this.contextURL = contextURL;
        this.tryLocalFirst = tryLocalFirst;
        this.proxiedServices = (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz}, new ServiceFailureDetection());
    }

    /**
     * Returns the service instance.
     *
     * @return The service
     * @throws javax.naming.NamingException
     * @throws java.rmi.RemoteException
     * @throws javax.ejb.CreateException
     */
    public synchronized T getService() throws NamingException, CreateException, RemoteException {
        checkServiceLoaded();
        return proxiedServices;
    }

    private void checkServiceLoaded() throws CreateException, NamingException, RemoteException {
        if (mustReload || services == null) {
            createServiceInstance();
        }
    }

    private synchronized void createServiceInstance() throws NamingException, RemoteException, CreateException {
        if (mustReload || services == null) {
            try {
                services = getServices();
                mustReload = false;
            } catch (Exception e) {
                log.info("Error getting services", e);
            }
        }
    }

    private T getServices() throws NamingException {
        InitialContext ctx = null;
        try {
            log.info("Creating new instance of " + iName);

            T ret = null;
            ctx = getContext();
            if (tryLocalFirst) {
                log.debug("lookup local " + localJNDIName);
                ret = (T) ctx.lookup(localJNDIName);
                if (ret != null) {
                    log.debug(ret);
                    log.debug("found locally");
                }
            }
            if (ret == null) {
                log.debug("lookup remote " + localJNDIName);
                ret = (T) ctx.lookup(remoteJNDIName);
                if (ret != null) {
                    log.debug("found remotely");
                }
            }
            return ret;
        } catch (NamingException e) {
            throw e;
        } finally {
            TCContext.close(ctx);
        }
    }


    /**
     * Returns the initial context to use for finding the Home
     *
     * @return The context
     * @throws NamingException
     */
    protected InitialContext getContext() throws NamingException {
        return TCContext.getInitial(contextURL);
    }

    private class ServiceFailureDetection implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                checkServiceLoaded();
                return method.invoke(services, args);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof RemoteException) {
                    log.warn(e.getTargetException().getClass().getName() + " when calling proxied method. home=" + iName);
                    mustReload = true;
                }
                throw e.getTargetException();
            }
        }
    }

}


