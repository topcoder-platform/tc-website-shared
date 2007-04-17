package com.topcoder.shared.messaging.messages;

/**
 * @author dok
 * @version $Revision$ Date: 2005/01/01 00:00:00
 *          Create Date: Feb 7, 2006
 */
public class AdminSubmitRequest extends BaseLongContestRequest {
    private long coderID;
    private long componentID;
    private long roundID;
    private long contestID;
    private int serverID;
    private int languageID;
    private String code;
    private boolean example;

    /**
     * @param coderID
     * @param componentID
     * @param roundID
     * @param contestID
     * @param languageID
     * @param serverID
     * @param code
     */
    public AdminSubmitRequest(long coderID, long componentID, long roundID,
                              long contestID, int languageID, int serverID, String code) {
        super();
        this.sync= false;
        this.coderID = coderID;
        this.componentID = componentID;
        this.roundID = roundID;
        this.contestID = contestID;
        this.languageID = languageID;
        this.serverID = serverID;
        this.code = code;
        this.example = false;
    }

    public AdminSubmitRequest(long coderID, long componentID, long roundID,
            long contestID, int languageID, int serverID, String code, boolean example) {
        super();
        this.sync= false;
        this.coderID = coderID;
        this.componentID = componentID;
        this.roundID = roundID;
        this.contestID = contestID;
        this.languageID = languageID;
        this.serverID = serverID;
        this.code = code;
        this.example = example;
    }

    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }
    /**
     * @return Returns the coderID.
     */
    public long getCoderID() {
        return coderID;
    }
    /**
     * @return Returns the componentID.
     */
    public long getComponentID() {
        return componentID;
    }
    /**
     * @return Returns the contestID.
     */
    public long getContestID() {
        return contestID;
    }
    /**
     * @return Returns the languageID.
     */
    public int getLanguageID() {
        return languageID;
    }
    /**
     * @return Returns the roundID.
     */
    public long getRoundID() {
        return roundID;
    }
    /**
     * @return Returns the serverID.
     */
    public int getServerID() {
        return serverID;
    }

    public boolean isExample() {
        return example;
    }
}


