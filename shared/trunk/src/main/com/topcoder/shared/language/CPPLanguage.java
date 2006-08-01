package com.topcoder.shared.language;

//import com.topcoder.netCommon.contest.ContestConstants;

public class CPPLanguage
    extends CStyleLanguage
{
    //public static int ID = ContestConstants.CPP;
    public final static int ID = 3;
    public final static String DESCRIPTION = "C++";

    public static CPPLanguage CPP_LANGUAGE = new CPPLanguage();

    public CPPLanguage()
    {
        super(ID, DESCRIPTION);
    }
    
    public String getDefaultExtension() {
        return "c";
    }
    
    public String exampleExposedCall(String className, String methodName, String[] paramNames) {
        StringBuffer buf = new StringBuffer();
        
        buf.append("val = ");
        buf.append(className);
        buf.append("::");
        buf.append(methodName);
        buf.append("(");
        
        for(int i = 0; i < paramNames.length; i++) {
            if(i > 0)
                buf.append(", ");
            buf.append(paramNames[i]);
        }
        
        buf.append(");");
        
        return buf.toString();
    }
}

