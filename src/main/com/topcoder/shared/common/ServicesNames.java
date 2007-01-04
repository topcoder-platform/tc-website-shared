/*
 * TestServicesNames
 * 
 * Created 11/29//2006
 */
package com.topcoder.shared.common;

/**
 * Services names container. <P>
 * 
 * Ad-hoc solution to avoid usage of ApplicationServer when using services
 * from the web app.
 * 
 * @author Diego Belfer (mural)
 * @version $Id$
 */
public interface ServicesNames {
    public final static String EMAIL_QUEUE = "eMailQueue";
    public final static String TEST_SERVICES = "jma.TestServicesHome";
    public final static String DB_SERVICES = "jma.DBServicesHome";
    public final static String TRACKING_SERVICES = "jma.TrackingServicesHome";
    public final static String MPSQAS_SERVICES = "jma.MPSQASServicesHome";
    public final static String PROBLEM_SERVICES = "jma.ProblemServicesHome";
    public final static String LOGIN_SERVICES = "security/Login";
    public final static String PACTS_CLIENT_SERVICES = "com.topcoder.web.ejb.pacts.PactsClientServicesHome";
}
