package com.topcoder.shared.ejb;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import java.rmi.RemoteException;

/**
 * @author dok
 * @version $Id$
 *          Create Date: Feb 27, 2008
 */
public interface ServiceLocator<T> {
    //todo refactor so that it throughs more general exceptions..these are pretty specific to EJB/JNDI
    T getService() throws NamingException, CreateException, RemoteException;
}
