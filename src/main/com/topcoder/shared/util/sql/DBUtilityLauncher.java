/*
 * Copyright (c) 2006 TopCoder, Inc. All rights reserved.
 */
package com.topcoder.shared.util.sql;

import com.topcoder.shared.util.logging.Logger;

/**
 * <strong>Purpose</strong>:
 * A launcher for the DBUtilities.
 *
 * The launcher should recieve as first param the DBUtility class to launch.
 *
 * @author pulky
 * @version 1.0.0
 */
public class DBUtilityLauncher {
    static Logger log = Logger.getLogger(DBUtilityLauncher.class);

    /**
     * The main method gets the DBUtility derived class to load, instanciate it and launch
     * the DBUtility process method.
     *
     * @param args The command line arguments list.
     */
    public static void main(String[] args) {
        String dbUtilityClass = args[0];
        if (dbUtilityClass == null) {
            log.info("Please specify in the first parameter the DBUtility class to launch.");
            return;
        }

        Class loadme = null;
        try {
            loadme = Class.forName(dbUtilityClass);
        } catch (Exception ex) {
            log.info("Unable to load class for utility: ");
            log.info(dbUtilityClass);
            log.info(". Cannot continue.\n");
            log.info(ex.getMessage());
        }

        Object ob = null;
        try {
            ob = loadme.newInstance();
            if (ob == null)
                throw new Exception("Object is null after newInstance call.");
        } catch (Exception ex) {
            log.info("Unable to create new instance of class for utility: ");
            log.info(dbUtilityClass);
            log.info(". Cannot continue.\n");
            log.info(ex.getMessage());
        }

        if (!(ob instanceof DBUtility)) {
            log.info(dbUtilityClass + " is not an instance of DBUtility. You must ");
            log.info("extend DBUtility to create a Database Utility.");
        }

        log.info("Launching " + dbUtilityClass + "...");
        
        DBUtility utility = (DBUtility) ob;
        utility.process(args);
    }
}
