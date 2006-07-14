/*
 * Copyright (c) 2006 TopCoder, Inc. All rights reserved.
 */
package com.topcoder.shared.util.sql;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.topcoder.shared.util.logging.Logger;

/**
 * <strong>Purpose</strong>:
 * A base class for building DB utilities.
 *
 * @author pulky
 * @version 1.0.0
 */
public abstract class DBUtility {
    /**
     * Logger to log to.
     */
    protected static Logger log = Logger.getLogger(DBUtility.class);

    /**
     * This holds all the parameters that have been parsed from the XML and the command line.
     */
    protected Hashtable params = new Hashtable();

    /**
     * This holds any error message that might occur when performing a particular
     * task.
     */
    protected StringBuffer sErrorMsg = new StringBuffer(128);

    /**
     * This variable holds the name of the JDBC driver we are using to connect
     * to the databases.
     */
    private String sDriverName = "com.informix.jdbc.IfxDriver";

    /**
     * This variable holds the source DB url.
     */
    private String sourcedb = null;

    /**
     * This variable holds the connection to the DB.
     */
    private Connection conn;

    /**
     * Runs the DBUtility.
     *
     * Subclasses should implemente this method to do whatever the utility needs to do. there will
     * be a parameters collection to look for and the connection to the DB will be resolved.
     */
    protected abstract void runUtility() throws Exception;

    /**
     * Show usage of the DBUtility.
     *
     * Subclasses should implemente this method to show how the final user should call them.
     *
     * @param msg The error message.
     */
    protected abstract void setUsageError(String msg);

    /**
     * Process the DBUtility.
     *
     * The utility first parses the xml and then the command line (overriting duplicated parameters),
     * then validates the parameters, checks the driver and starts the utility.
     *
     * @param args command line arguments
     */
    protected void process(String[] args) {
        if (args.length > 2 && args[1].equals("-xmlfile")) {
            parseXML(args[2]);
        }
        parseArgs(args);
        processParams();
        checkDriver();
        startUtility();
    }

    /**
     * Call this method to create a PreparedStatement for a given sql.
     *
     * @param sqlStr The sql query.
     */
    protected PreparedStatement prepareStatement(String sqlStr) throws SQLException {
        if (conn == null)
            return null;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sqlStr);
        } catch (SQLException sqle) {
            throw sqle;
        }
        return ps;
    }

    /**
     * Aborts the utility and show the causing error.
     */
    protected void fatal_error() {
        log.error("*******************************************");
        log.error("FAILURE: " + sErrorMsg.toString());
        log.error("*******************************************");
        System.exit(-1);
    }

    /**
     * Aborts the utility and show the causing error.
     *
     * @param e The exception causing the fatal error.
     */
    protected void fatal_error(Exception e) {
        log.error("*******************************************");
        log.error("FAILURE: ", e);
        log.error("*******************************************");
        System.exit(-1);
    }

    /**
     * This method performs a Class.forName on the driver used for this
     * utility.
     */
    protected void checkDriver() {
        try {
            Class.forName(sDriverName);
        } catch (Exception ex) {
            sErrorMsg.setLength(0);
            sErrorMsg.append("Unable to load driver ");
            sErrorMsg.append(sDriverName);
            sErrorMsg.append(". Cannot continue.");
            fatal_error();
        }
    }

    /**
     * This method parses all parameters specified by the XML file
     * passed on the command line.
     *
     * @param xmlFileName The xml file name.
     */
    protected void parseXML(String xmlFileName) {
        try {
            FileInputStream f = new FileInputStream(xmlFileName);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder dombuild = dbf.newDocumentBuilder();
            Document doc = dombuild.parse(f);

            Element root = doc.getDocumentElement();
            NodeList nl = root.getChildNodes();

            for (int i = 1; i < nl.getLength(); i += 1) {
                Node node = nl.item(i);
                if (node.getNodeName() != null && node.getNodeName() != "#text") {
                    if (node.getNodeName().equals("parameterList")) {
                        NodeList nl2 = node.getChildNodes();
                        for (int j = 1; j < nl2.getLength(); j += 2) {
                            Node n2 = nl2.item(j);
                            NamedNodeMap nnm = n2.getAttributes();
                            params.put(nnm.getNamedItem("name").getNodeValue(),
                                    nnm.getNamedItem("value").getNodeValue());
                        }
                    } else {
                        params.put(node.getNodeName(), node.getFirstChild().getNodeValue());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            sErrorMsg.setLength(0);
            sErrorMsg.append("Load of XML file failed:\n");
            sErrorMsg.append(ex.getMessage());
            fatal_error(ex);
        }
    }

    /**
     * This method converts an array of Strings into a Hashtable of
     * arguments. The arguments form keys seperated by a -. So, an
     * argument list of "-test one -test2 two" will create a Hashtable
     * with two keys, "test" and "test2" with corresponding values of
     * "one" and "two".
     *
     * @param args The command line arguments.
     */
    protected void parseArgs(String[] args) {
        for (int i = 1; i < args.length - 1; i += 2) {
            if (!args[i].startsWith("-")) {
                sErrorMsg.setLength(0);
                sErrorMsg.append("Argument " + (i + 1) + " (" + args[i] + ") should start with a -.");
                fatal_error();
            }

            String key = args[i].substring(1);
            String value = args[i + 1];

            if (!args[i].equals("-xmlfile")) {
                params.put(key, value);
            }
        }
    }

    /**
     * Process and validates the parameters.
     */
    protected void processParams() {
        String tmp = (String) params.get("driver");
        if (tmp != null) {
            sDriverName = tmp;
            params.remove("driver");
        }

        sourcedb = (String) params.get("sourcedb");
        if (sourcedb == null)
            setUsageError("Please specify a database url.\n");

        params.remove("sourcedb");

        log.debug("processParams");
        log.debug("sDriverName : " + sDriverName);
        log.debug("sourcedb" + sourcedb);
    }

    /**
     * This method creates the connection and invoke the particular DBUtility method.
     */
    protected void startUtility() {
        try {
            log.info("Creating source database connection...");
            conn = DriverManager.getConnection(sourcedb);
            System.out.println(conn);
            log.info("Success!");
        } catch (SQLException sqle) {
            sErrorMsg.setLength(0);
            sErrorMsg.append("Creation of source DB connection failed. ");
            sErrorMsg.append("Cannot continue.\n");
            sErrorMsg.append(sqle.getMessage());
            fatal_error(sqle);;
        }

        try {
            runUtility();
        } catch (Exception e) {
            fatal_error(e);
        }
    }
}
