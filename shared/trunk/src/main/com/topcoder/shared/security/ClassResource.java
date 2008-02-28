package com.topcoder.shared.security;

/**
 * Resource which represents a class, as the full name with package.
 *
 * @author Ambrose Feinstein,dok
 * @version $Id$
 */
public class ClassResource implements Resource {
    private static final long serialVersionUID = 666L;

    private String name;

    public ClassResource(Class c) {
        this.name = c.getName();
    }

    public String getName() {
        return name;
    }
}
