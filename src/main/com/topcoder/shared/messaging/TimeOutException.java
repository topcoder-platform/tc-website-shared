package com.topcoder.shared.messaging;

import com.topcoder.shared.util.TCException;
import com.topcoder.web.common.TCWebException;

/**
 * User: dok
 * Date: Dec 10, 2004
 * Time: 3:57:07 PM
 */
public class TimeOutException extends TCWebException {

    /**
     * Default Constructor
     */
    public TimeOutException() {
        super();
    }

    /**
     * <p>
     * Constructor taking a string message
     * </p>
     *
     * @param message - the message of the exception
     */
    public TimeOutException(String message) {
        super(message);
    }

    /**
     * <p>
     * Constructor taking a nested exception
     * </p>
     *
     * @param nestedException the nested exception
     */
    public TimeOutException(Throwable nestedException) {
        super(nestedException);
    }

    /**
     * <p>
     * Constructor taking a nested exception and a string
     * </p>
     *
     * @param message the message of this exception
     * @param nestedException the nested exception
     */
    public TimeOutException(String message, Throwable nestedException) {
        super(message, nestedException);
    }

}
