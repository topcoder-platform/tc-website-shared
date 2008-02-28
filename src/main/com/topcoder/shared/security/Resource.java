package com.topcoder.shared.security;

import org.jboss.cache.aop.annotation.Serializable;

/**
 * interface for resources, they could be web pages, web applications
 * etc.  anything that one might require authentication for access to.
 *
 * @author dok
 * @version $Id$
 */
public interface Resource extends Serializable {

    public String getName();
}
