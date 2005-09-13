/*
 * CacheClearer.java
 *
 * Created on September 13, 2005, 11:33 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.topcoder.shared.util.dwload;

/**
 *
 * @author rfairfax
 */
public class CacheClearer {
    
    /** Creates a new instance of CacheClearer */
    public CacheClearer() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        TCLoadTCS load = new TCLoadTCS();
        try {
            load.doClearCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
