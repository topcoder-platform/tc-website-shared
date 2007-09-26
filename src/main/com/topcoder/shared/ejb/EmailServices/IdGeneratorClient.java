package com.topcoder.shared.ejb.EmailServices;

import com.topcoder.shared.util.DBMS;
import com.topcoder.shared.util.logging.Logger;
import com.topcoder.util.idgenerator.IDGenerationException;
import com.topcoder.util.idgenerator.IDGenerator;
import com.topcoder.util.idgenerator.IDGeneratorFactory;

/**
 * @author dok
 * @version $Revision$ Date: 2005/01/01 00:00:00
 *          Create Date: Sep 26, 2007
 */
public class IdGeneratorClient {

    private static Logger log = Logger.getLogger(IdGeneratorClient.class);


    /**
     * Uses the IdGenerator class to retrieve a sequence value for the
     * sequence name given. Will initialize the IdGenerator if not initialized
     * yet.
     *
     * @param seqName
     * @return The next sequence val. -1 if there is an exception thrown
     *         or other error retrieving the sequence id.
     */

    public static long getSeqId(String seqName) throws IDGenerationException {
        if (log.isDebugEnabled()) {
            log.debug("getSeqId(" + seqName + ") called");
        }
        long ret = getSeqId(seqName, DBMS.COMMON_OLTP_DATASOURCE_NAME);
        if (log.isDebugEnabled()) {
            log.debug("returning " + ret);
        }
        return ret;
    }

    private static long getSeqId(String seqName, String dataSourceName) throws IDGenerationException {
/*
        if (log.isDebugEnabled()) {
            log.debug("getSeqId(" + seqName + ", " + dataSourceName + ") called");
        }
*/
        long retVal;
        IDGenerator gen = IDGeneratorFactory.getIDGenerator(seqName);
        retVal = gen.getNextID();
        //System.out.println("retVal = " + retVal);
        return retVal;
    }

}