package com.topcoder.shared.language;

import com.topcoder.shared.problem.DataType;

/**
 * Represents the Python language implementation of the Language Interface.
 *
 * @see     Language
 * @see     CStyleLanguage
 */

// Note: this is part of the plugin API javadoc.  Please be sure to
// keep the javadoc comments up to date.  When implementing changes,
// be sure to regenerate/repackage the plugin API javadoc.

public class PythonLanguage extends CStyleLanguage {

    /** Identifier for the Python language. */
    public final static int ID = 6;

    /** Descriptor for the Python language. */
    public final static String DESCRIPTION = "Python";

    /** Singleton instance for the Python language. */
    public final static PythonLanguage PYTHON_LANGUAGE = new PythonLanguage();

    /** Constructor of the Python language. */
    public PythonLanguage() { 
        super(ID, DESCRIPTION);
    }

    public String getMethodSignature(String methodName, DataType returnType, DataType[] paramTypes, String[] paramNames) {
        if (paramTypes.length != paramNames.length)
            throw new RuntimeException("PythonLanguage.getMethodSignature: paramTypes.length != paramNames.length (" +
                    paramTypes.length + " + " + paramNames.length + ")");

        String returns = returnType.getDescriptor(this);
        String[] params = new String[paramTypes.length];
        int len = returns.length() + methodName.length() + 3; // 3 = ' ' + '(' + ')'

        for (int i = 0; i < params.length; i++) {
            paramTypes[i].getDescriptor(this);
            params[i] = paramNames[i];
            len += params[i].length();
        }
        len += 2 * (params.length - 1);

        StringBuffer buf = new StringBuffer(len);

        buf.append("def ");
        buf.append(methodName);
        buf.append("(self, ");
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(params[i]);
        }
        buf.append("):");
        return buf.toString();
    }
    
    public String exampleExposedCall(String className, String methodName, String[] paramNames) {
        StringBuffer buf = new StringBuffer();
        
        buf.append("val = ");
        buf.append(className);
        buf.append(".");
        buf.append(methodName);
        buf.append("(");
        
        for(int i = 0; i < paramNames.length; i++) {
            if(i > 0)
                buf.append(", ");
            buf.append(paramNames[i]);
        }
        
        buf.append(")");
        
        return buf.toString();
    }
    
    public String getDefaultExtension() {
        return "py";
    }
}