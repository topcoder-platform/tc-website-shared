package com.topcoder.shared.language;

import com.topcoder.shared.problem.DataType;

/**
 * @author dok
 * @version $Revision$ Date: 2005/01/01 00:00:00
 *          Create Date: Jul 20, 2006
 */
public class PythonLanguage extends BaseLanguage {

    public final static int ID = 6;
    public final static String DESCRIPTION = "Python";

    public final static PythonLanguage PYTHON_LANGUAGE = new PythonLanguage();

    public PythonLanguage() {
        super(ID, DESCRIPTION);
    }

    public String getMethodSignature(String methodName, DataType returnType, DataType[] paramTypes, String[] paramNames) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
