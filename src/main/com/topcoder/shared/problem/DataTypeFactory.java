package com.topcoder.shared.problem;

import java.util.Collection;
import java.util.HashMap;

import com.topcoder.shared.util.DBMS;

import java.sql.*;
/**
 * This class implements a global database of known data types.  Ideally it would be populated
 * at some appropriate initialization time with the set of valid data types.  Construction of
 * any new <code>DataType</code> adds to the data type population.
 *
 * @author Logan Hanks
 * @see DataType
 */
public class DataTypeFactory {
    static private HashMap types = new HashMap();

    private final static Logger log =  Logger.getLogger(DataTypeFactory.class);
    
    static private boolean initialized = false;
    
    static public void initialize() {
        if(initialized)
            return;
               
        Connection conn = null;
        PreparedStatement s = null;
        ResultSet rs = null;
        try {
            conn = DBMS.getConnection();
            s = conn.prepareStatement(
                    "SELECT data_type_id, language_id, display_value "
                    + "FROM data_type_mapping");
            rs = s.executeQuery();
            HashMap mappings = new HashMap();

            while (rs.next()) {
                int dataTypeId = rs.getInt(1);
                int languageId = rs.getInt(2);
                String desc = rs.getString(3);
                HashMap mapping = (HashMap) mappings.get(new Integer(dataTypeId));

                if (mapping == null) {
                    mapping = new HashMap();
                    mappings.put(new Integer(dataTypeId), mapping);
                }
                mapping.put(new Integer(languageId), desc);
            }
            rs.close();
            s.close();
            s = conn.prepareStatement("SELECT data_type_id, data_type_desc "
                    + " FROM data_type");
            rs = s.executeQuery();
            while (rs.next()) {
                new DataType(rs.getInt(1), rs.getString(2),
                        (HashMap) mappings.get(new Integer(rs.getInt(1))));

            }
            
            initialized = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            DBMS.close(conn, s, rs);
        }
    }
    
    /**
     * Obtain a <code>DataType</code> object with the given description.
     *
     * @param description A type description (e.g. <code>"String[]"</code>)
     * @throws InvalidTypeException
     */
    static public DataType getDataType(String description)
            throws InvalidTypeException {
        DataTypeFactory.initialize();
        
        DataType type = (DataType) types.get(description);

        if (type == null)
            throw new InvalidTypeException(description);
        return type.cloneDataType();
    }

    static public DataType getDataType(int typeID)
            throws InvalidTypeException {
        DataTypeFactory.initialize();
        
        DataType type = (DataType) types.get(new Integer(typeID));

        if (type == null)
            throw new InvalidTypeException(new String("" + typeID));
        return type.cloneDataType();
    }

    static void registerDataType(DataType type) {
        initialized = true;
        if (types.containsKey(type.getDescription()))
            return;
        types.put(type.getDescription(), type.cloneDataType());
        types.put(new Integer(type.getID()), type.cloneDataType());
    }

    static public Collection getDataTypes() {
        DataTypeFactory.initialize();
        
        return types.values();
    }
}

