package com.topcoder.shared.problem;

import com.topcoder.shared.netCommon.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

//import java.util.Iterator;

public class SimpleComponent
        implements Serializable, CustomSerializable {

    int problemID;
    int componentID;
    int componentTypeID;
    String className;
    String methodName;
    DataType[] paramTypes;
    DataType returnType;
    Long[] webServiceDependencies = null;

    public SimpleComponent() {
    }

	/** Custom serialization */
    public void customWriteObject(CSWriter writer)
            throws IOException {
        writer.writeString(className);
        writer.writeString(methodName);
        writer.writeObject(returnType);
        writer.writeObjectArray(paramTypes);
        writer.writeInt(componentID);
        writer.writeInt(componentTypeID);
        writer.writeInt(problemID);
        writer.writeObjectArray(webServiceDependencies);
    }

    public void customReadObject(CSReader reader)
            throws IOException {
        Object[] o_paramTypes;

        className = reader.readString();
        methodName = reader.readString();
        returnType = (DataType) reader.readObject();
        o_paramTypes = reader.readObjectArray();
        componentID = reader.readInt();
        componentTypeID = reader.readInt();
        problemID = reader.readInt();

        if (o_paramTypes == null)
            o_paramTypes = new Object[0];
        paramTypes = new DataType[o_paramTypes.length];
        for (int i = 0; i < o_paramTypes.length; i++)
            paramTypes[i] = (DataType) o_paramTypes[i];
        webServiceDependencies = (Long[]) reader.readObjectArray(Long.class);
    }

    public int getProblemID() {
        return problemID;
    }

    public void setProblemID(int problemID) {
        this.problemID = problemID;
    }

    public int getComponentID() {
        return componentID;
    }

    public void setComponentID(int componentID) {
        this.componentID = componentID;
    }

    public int getComponentTypeID() {
        return componentTypeID;
    }

    public void setComponentTypeID(int componentTypeID) {
        this.componentTypeID = componentTypeID;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public DataType[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(DataType[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public DataType getReturnType() {
        return returnType;
    }

    public void setReturnType(DataType returnType) {
        this.returnType = returnType;
    }

    public static String getCacheKey(int componentID) {
        return "SimpleProblemComponent." + componentID;
    }

    public String getCacheKey() {
        return "SimpleProblemComponent." + componentID;
    }

    /**
     * @param language the languageID
     * @return the return type for the languageID
     */
    public String getReturnType(int language) {
        return returnType.getDescriptor(language);
    }

    public boolean hasWebServiceDependencies() {
        return webServiceDependencies != null && webServiceDependencies.length > 0;
    }

    public Long[] getWebServiceDependencies() {
        return webServiceDependencies;
    }

    public void setWebServiceDependencies(Long[] webServiceIDs) {
        webServiceDependencies = webServiceIDs;
    }

    public String toString() {
        StringBuffer ret = new StringBuffer(1000);
        ret.append("(com.topcoder.shared.problem.SimpleComponent) [");
        ret.append("problemID = ");
        ret.append(problemID);
        ret.append(", ");
        ret.append("componentID = ");
        ret.append(componentID);
        ret.append(", ");
        ret.append("componentTypeID = ");
        ret.append(componentTypeID);
        ret.append(", ");
        ret.append("className = ");
        if (className == null) {
            ret.append("null");
        } else {
            ret.append(className.toString());
        }
        ret.append(", ");
        ret.append("methodName = ");
        if (methodName == null) {
            ret.append("null");
        } else {
            ret.append(methodName.toString());
        }
        ret.append(", ");
        ret.append("paramTypes = ");
        if (paramTypes == null) {
            ret.append("null");
        } else {
            ret.append("{");
            for (int i = 0; i < paramTypes.length; i++) {
                ret.append(paramTypes[i].toString() + ",");
            }
            ret.append("}");
        }
        ret.append(", ");
        ret.append("returnType = ");
        if (returnType == null) {
            ret.append("null");
        } else {
            ret.append(returnType.toString());
        }
        ret.append(", ");
        ret.append("webServiceDependencies = ");
        if (webServiceDependencies == null) {
            ret.append("null");
        } else {
            ret.append(Arrays.asList(webServiceDependencies));
        }
        ret.append(", ");
        ret.append("]");
        return ret.toString();
    }
}
