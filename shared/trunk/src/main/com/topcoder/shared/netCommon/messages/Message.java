/**
 * Message.java
 *
 * Description:		Defines common behaviors of a message
 * @author			Tim "Pops" Roberts
 * @version			1.0
 */

package com.topcoder.shared.netCommon.messages;

//import com.topcoder.netCommon.*;

import com.topcoder.shared.netCommon.*;

import java.io.*;

public abstract class Message implements Serializable, Cloneable, CustomSerializable {

    /** Default Constructor */
    public Message() {
    }

    /** Default customWriteObject */
    public void customWriteObject(CSWriter writer) throws IOException {
    }

    /** Default customReadObject */
    public void customReadObject(CSReader reader) throws IOException {
    }

    /**
     * Gets the string representation of this object
     *
     * @return the string representation of this object
     */
    public String toString() {
        return "(Message)[]";
    }
}


/* @(#)Message.java */
