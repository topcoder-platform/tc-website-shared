package com.topcoder.shared.language;

import java.io.Serializable;

import com.topcoder.shared.netCommon.CustomSerializable;
import com.topcoder.shared.problem.DataType;


/**
 * The interface for the implementation of all the
 * semantics associated with a supported programming language.  This
 * basically consists of the logic for generating language-dependent
 * method signatures.  An instance of type <code>Language</code> also
 * serves as a convenient identifier for a particular language.
 *
 * @author  Logan Hanks
 * @see     DataType
 */
public interface Language 
    extends Serializable, Cloneable, CustomSerializable {

    int getId();

    String getName();

    /** Two languages are equal if they have the same id */
    boolean equals(Object o);
      
    String getMethodSignature(String methodName, DataType returnType,
                                              DataType[] paramTypes, String[] paramNames);
    
    String exampleExposedCall(String className, String methodName, String[] paramNames);

    /**
     * @return the default file extension for the language
     */
    String getDefaultExtension();
}

