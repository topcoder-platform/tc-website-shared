package com.topcoder.shared.problem;

import java.io.IOException;
import java.io.ObjectStreamException;

import com.topcoder.shared.netCommon.CSReader;
import com.topcoder.shared.netCommon.CSWriter;

/**
 * A user constraint is where the problem writer writes arbitrary text to specify
 * a constraint.  This class is basically just a container for some <code>Element</code>.
 *
 * @see Element
 * @author Logan Hanks
 */
public class UserConstraint
        extends Constraint {
    private Element elem;

    public UserConstraint() {
    }

    public UserConstraint(String text) {
        super("");
        this.elem = new StructuredTextElement("user-constraint", text);
    }

    public UserConstraint(Element elem) {
        super("");
        this.elem = elem;
    }

    public void customWriteObject(CSWriter writer)
            throws IOException {
        super.customWriteObject(writer);
        writer.writeObject(elem);
    }

    public void customReadObject(CSReader reader)
            throws IOException, ObjectStreamException {
        super.customReadObject(reader);
        elem = (Element) reader.readObject();
    }
    
    public String toXML() {
        return elem.toXML();
    }

    public Element getUserConstraint() {
        return elem;
    }
    
    public String getText() {
        return elem.toString();
    }
}
