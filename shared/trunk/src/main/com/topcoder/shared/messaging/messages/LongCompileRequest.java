package com.topcoder.shared.messaging.messages;

import java.io.Serializable;

public class LongCompileRequest implements Serializable{
    private int coderID, componentID, roundID, contestID, languageID, serverID;
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
    public LongCompileRequest(int coderID, int componentID, int roundID,
            int contestID, int languageID, int serverID, String code) {
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
    public int getCoderID() {
        return coderID;
    }
    /**
     * @return Returns the componentID.
     */
    public int getComponentID() {
        return componentID;
    }
    /**
     * @return Returns the contestID.
     */
    public int getContestID() {
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
    public int getRoundID() {
        return roundID;
    }
    /**
     * @return Returns the serverID.
     */
    public int getServerID() {
        return serverID;
    }
}