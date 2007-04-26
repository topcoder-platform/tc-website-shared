package com.topcoder.shared.security;

import com.topcoder.shared.util.logging.Logger;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

/**
 * Simple immutable container for a User.
 *
 * @author Ambrose Feinstein
 */
public class SimpleUser implements User, HttpSessionBindingListener {

    private static final Logger log = Logger.getLogger(SimpleUser.class);

    private static int GUEST_ID = -1;
    private static String GUEST_NAME = "anonymous";

    private long id;
    private String username;
    private String password;

    /** Construct a User object having the given values. */
    public SimpleUser(long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public String getUserName() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAnonymous() {
        return id == GUEST_ID;
    }

    public static User createGuest() {
        return new SimpleUser(GUEST_ID, GUEST_NAME, "");
    }

    public void valueBound(HttpSessionBindingEvent httpSessionBindingEvent) {
        log.debug("user " + username + " bound");
    }

    public void valueUnbound(HttpSessionBindingEvent httpSessionBindingEvent) {
        log.debug("user " + username + " un bound");
    }
}
