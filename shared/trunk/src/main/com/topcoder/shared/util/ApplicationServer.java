package com.topcoder.shared.util;

import com.topcoder.shared.util.logging.Logger;

import javax.naming.Context;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * A class to hold application wide constants
 *
 * @author Steve Burrows
 * @version $Revision$
 */
public class ApplicationServer {
    private static Logger log = Logger.getLogger(ApplicationServer.class);

    private static TCResourceBundle bundle = new TCResourceBundle("ApplicationServer");

    public static final int PROD = bundle.getIntProperty("PROD", 1);
    public static final int QA = bundle.getIntProperty("QA", 2);
    public static final int DEV = bundle.getIntProperty("DEV", 3);

    public static int ENVIRONMENT = bundle.getIntProperty("ENVIRONMENT", DEV);
    public static String SERVER_NAME = bundle.getProperty("SERVER_NAME", "172.16.20.20");
    public static String STUDIO_SERVER_NAME = bundle.getProperty("STUDIO_SERVER_NAME", "studio.dev.topcoder.com");
    public static String CSF_SERVER_NAME = bundle.getProperty("CSF_SERVER_NAME", "csf.dev.topcoder.com");
    public static String CORP_SERVER_NAME = bundle.getProperty("CORP_SERVER_NAME", "172.16.20.20/corp");
    public static String SOFTWARE_SERVER_NAME = bundle.getProperty("SOFTWARE_SERVER_NAME", "172.16.20.222");
    public static String FORUMS_SERVER_NAME = bundle.getProperty("FORUMS_SERVER_NAME", "forums.topcoder.com");
    public static String HOST_URL = bundle.getProperty("HOST_URL", "t3://172.16.20.41:7030");
    public static String FORUMS_HOST_URL = bundle.getProperty("FORUMS_HOST_URL", "63.118.154.182:1099");
    public static String JMS_HOST_URL = bundle.getProperty("JMS_HOST_URL", "jnp://172.16.210.55:1100,jnp://172.16.210.56:1100");
    public static String CONTEST_HOST_URL = bundle.getProperty("CONTEST_HOST_URL", "t3://172.16.20.40:9003");
    public static String PACTS_HOST_URL = bundle.getProperty("PACTS_HOST_URL", "t3://172.16.20.40:9003");
    public static String BASE_DIR = bundle.getProperty("BASE_DIR", "/usr/web/build/classes");
    public static String SECURITY_PROVIDER_URL = bundle.getProperty("SECURITY_PROVIDER_URL", "172.16.20.40:1099");
    public static String FILE_CONVERSION_PROVIDER_URL = bundle.getProperty("FILE_CONVERSION_PROVIDER_URL", "172.16.210.53:1099");


    public final static String JNDI_FACTORY = bundle.getProperty("JNDI_FACTORY", "weblogic.jndi.WLInitialContextFactory");
    public final static String JMS_FACTORY = bundle.getProperty("JMS_FACTORY", "jms.connection.jmsFactory");
    public final static String JMS_BKP_FACTORY = bundle.getProperty("JMS_BKP_FACTORY", "jms.connection.jmsFactory_BKP");
    public final static String TRANS_FACTORY = bundle.getProperty("TRANS_FACTORY", "javax.transaction.UserTransaction");
    public final static String TRANS_MANAGER = bundle.getProperty("TRANS_MANAGER", "weblogic/transaction/TransactionManager");
    public final static String SECURITY_CONTEXT_FACTORY = bundle.getProperty("SECURITY_CONTEXT_FACTORY", "org.jnp.interfaces.NamingContextFactory");
    public final static int SESSION_ID_LENGTH = bundle.getIntProperty("SESSION_ID_LENGTH", 50);

    public static String TCS_APP_SERVER_URL = bundle.getProperty("TCS_APP_SERVER_URL", "172.16.20.222:1099");

    public final static int WEB_SERVER_ID = bundle.getIntProperty("WEB_SERVER_ID", 1);
    
    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception ignore) {
                log.error("FAILED to close ResultSet.");
                ignore.printStackTrace();
            }
        }
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception ignore) {
                log.error("FAILED to close Connection.");
                ignore.printStackTrace();
            }
        }

    }

    public static void close(Context ctx) {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (Exception ignore) {
                log.error("FAILED to close Context.");
                ignore.printStackTrace();
            }
        }

    }

    public static void close(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (Exception ignore) {
                log.error("FAILED to close PreparedStatement.");
                ignore.printStackTrace();
            }
        }

    }


}
