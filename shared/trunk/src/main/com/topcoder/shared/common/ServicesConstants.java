package com.topcoder.shared.common;

//import com.topcoder.server.common.ApplicationServer;

import com.topcoder.shared.util.SimpleResourceBundle;

/**
 * ServicesConstants.java
 *
 * Created on October 22, 2001
 */

/**
 * Contains constant variables for the services package.
 *
 * @author Alex Roman
 * @version 1.0
 */
public final class ServicesConstants {

    private ServicesConstants() {
    }

    private static final SimpleResourceBundle bundle = SimpleResourceBundle.getBundle("Tester");

    /** Run the services in simluation mode...no EJB calls and JMS activity */
    public static final boolean SIM = false;

    /** base directory of the services package */
    private static final String BASE = bundle.getString("BASE");

    /** base directory of c++ problem solutions, and boundary checkers */
    public static final String SOLUTIONS = BASE + "/solutions/";

    /** base directory of java problem user submissions */
    public static final String JAVA_SUBMISSIONS = BASE + "/submissions/java/";
    public static final String PYTHON_SUBMISSIONS = BASE + "/submissions/python/";

    public static final String PYTHON_COMPILER = BASE + "/src/python/compiler.py";

    public static final String CSHARP_SUBMISSIONS = BASE + "/submissions/csharp/";

    /** base directory of c++ problem user submissions */
    public static final String CPP_SUBMISSIONS = BASE + "/submissions/cpp/";

    public static final String LONG_SUBMISSIONS = BASE + "/submissions/long/";

    public static final String APPS_CLASSES = BASE + "/build/classes.jar";
    public static final String RESOURCES_FOLDER = BASE + "/resources";
    public static final String TESTER_POLICY = BASE + "/scripts/services/tester.policy";
    public static final String DOTNET_BIN = BASE + "/build/dotNet";
    public static final String LONG_DOTNET_IO = BASE + "/src/csharp/LongTesterIO.cs";
    public static final String DOTNET_SANDBOX = DOTNET_BIN + "/LongWrapper.exe";
    public static final String DOTNET_TESTER = DOTNET_BIN + "/TestProcess.exe";

    /** Long contest wrappers */
    public static final String LONG_CONTEST_MPSQAS_WRAPPER = BASE + "/wrapper/lcontest/LongContestMPSQASWrapper.java";
    public static final String LONG_CONTEST_USER_WRAPPER = BASE + "/wrapper/lcontest/LongContestUserWrapper.java";
    public static final String LONG_CONTEST_CPP_USER_WRAPPER = BASE + "/wrapper/lcontest/LongContestUserWrapper.cpp";
    public static final String LONG_CONTEST_DOTNET_USER_WRAPPER = BASE + "/wrapper/lcontest/LongContestUserWrapper.cs";
    public static final String LONG_CONTEST_PYTHON_USER_WRAPPER = BASE + "/wrapper/lcontest/LongContestUserWrapper.py";

    public static final String LONG_CONTEST_DOTNET_EXPOSED_WRAPPER = BASE + "/wrapper/lcontest/LongContestExposedWrapper.cs";
    public static final String LONG_CONTEST_EXOPOSED_WRAPPER = BASE + "/wrapper/lcontest/LongContestExposedWrapper.java";
    public static final String LONG_CONTEST_CPP_EXPOSED_WRAPPER = BASE + "/wrapper/lcontest/LongContestExposedWrapper.cpp";
    public static final String LONG_CONTEST_PYTHON_EXPOSED_WRAPPER = BASE + "/wrapper/lcontest/LongContestExposedWrapper.py";

    public static final String PYTHON_WRAPPER = BASE + "/wrapper/srm/PythonUserWrapper.py";

    /** argparser.h C++ header file */
    public static final String WRAPPER = BASE + "/cpp/wrapper.cc";
    public static final String LONG_WRAPPER = BASE + "/cpp/long_wrapper.cc";
    public static final String LONG_IO = BASE + "/cpp/long_io.cc";

    /** The directory path to the C++ sandbox application */
    public static final String SANDBOX = BASE + "/cpp/sandbox/sandbox";
    public static final String SANDBOX2 = BASE + "/cpp/sandbox2/GNU-i386-Linux/sandbox2";

    public static final String SANDBOX2_SRM_CONFIG = BASE + "/cpp/sandbox2/config_files/sample.config";

    public static final String SANDBOX2_LONG_CONFIG = BASE + "/cpp/sandbox2/config_files/long.config";

    public static final String SANDBOX2_THREADED_LONG_CONFIG = BASE + "/cpp/sandbox2/config_files/tlong.config";

    public static final String SANDBOX2_LONG_PYTON_CONFIG = BASE + "/cpp/sandbox2/config_files/long_python.config";

    public static final String SANDBOX2_PYTHON_CONFIG = BASE + "/cpp/sandbox2/config_files/python.config";

    public static final String LONG_SANDBOX = BASE + "/cpp/sandbox/long_sandbox";

    /** The directory path to the timeout application */
    public static final String ALARM = BASE + "/cpp/timeout/timeout";

    public static final String RUN = BASE + "/cpp/run.sh";
    public static final String RUN_JAVA = BASE + "/cpp/runjava.sh";
    public static final String PKILL = BASE + "/pkill.sh";

    /** The package name of the contest solutions directory */
    //public static String SOLUTIONS_PACKAGE = "cpp.solutions.";
    public static final String SOLUTIONS_PACKAGE = "com.topcoder.tester.solutions.";
    //public static String SOLUTIONS_PACKAGE = "solutions.";

    public static final int CONTEST_COMPILE_ACTION = 1;
    public static final int MPSQAS_COMPILE_ACTION = 2;
    public static final int LONG_COMPILE_ACTION = 11;

    public static final int USER_TEST_ACTION = 3;
    public static final int CHALLENGE_TEST_ACTION = 4;
    public static final int SYSTEM_TEST_ACTION = 5;
    public static final int PRACTICE_TEST_ACTION = 6;
    public static final int MPSQAS_TEST_ACTION = 7;
    public static final int WEB_SERVICE_DEPLOY_ACTION = 8;
    public static final int AUTO_TEST_ACTION = 9;
    public static final int LONG_TEST_ACTION = 10;
    public static final int LONG_SYSTEM_TEST_ACTION = 12;

    public static final int FAILURE_INCORRECT_RESULT = 1;
    public static final int FAILURE_TIMEOUT = 2;

    public static final int MAX_LONG_SUBMISSIONS = bundle.getInt("MAX_LONG_SUBMISSIONS");
    public static final int LONG_SUBMISSION_INTERVAL = bundle.getInt("LONG_SUBMISSION_INTERVAL");

    public static final int MARATHON_PORT_NUMBER = System.getProperty("MARATHON_PORT_NUMBER") == null ? 8000 : Integer.parseInt(System.getProperty("MARATHON_PORT_NUMBER"));

}
