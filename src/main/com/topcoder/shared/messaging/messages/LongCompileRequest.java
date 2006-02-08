package com.topcoder.shared.messaging.messages;

public class LongCompileRequest extends BaseLongContestRequest {
    private long coderID, componentID, roundID, contestID;
    private int languageID, serverID;
    private String code;

    /**
     * @param coderID
     * @param componentID
     * @param roundID
     * @param contestID
     * @param languageID
     * @param serverID
     * @param code
     */
    public LongCompileRequest(long coderID, long componentID, long roundID,
                              long contestID, int languageID, int serverID, String code) {
        super();
        this.coderID = coderID;
        this.componentID = componentID;
        this.roundID = roundID;
        this.contestID = contestID;
        this.languageID = languageID;
        this.serverID = serverID;
        this.code = code;
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
}