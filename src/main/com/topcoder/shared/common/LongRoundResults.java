package com.topcoder.shared.common;

import java.io.Serializable;
import java.util.*;

public class LongRoundResults implements Serializable {
    private ArrayList scores, testCaseIds, coderIds, coderHandles, finalScores;
    private int componentID, roundID;
    private String className;
    private ArrayList records;
    private int[] so;
    public LongRoundResults(ArrayList scores, ArrayList testCaseIds, ArrayList coderIds, ArrayList finalScores, ArrayList coderHandles,  int componentID, int roundID){
        this.scores = scores;
        this.testCaseIds = testCaseIds;
        this.coderIds = coderIds;
        this.finalScores = finalScores;
        this.coderHandles = coderHandles;
        this.componentID = componentID;
        this.roundID = roundID;
        records = new ArrayList();
        for(int i = 0; i<scores.size(); i++){
            ArrayList al = (ArrayList)scores.get(i);
            double score = Double.NaN;
            int cr = ((Integer)coderIds.get(i)).intValue();
            String handle = null;
            if(finalScores != null)
                score = ((Double)finalScores.get(i)).doubleValue();
            if(coderHandles != null)
                handle = (String)coderHandles.get(i);
            records.add(new Record(cr,handle,score,al));

        }
    }
    public LongRoundResults(ArrayList s, ArrayList tc, ArrayList c, int componentID, int roundID){
        this(s,tc,c,null,null,componentID, roundID);
    }
    public int getComponentID(){
        return componentID;
    }
    public int getRoundID(){
        return roundID;
    }
    public void setFinalScores(ArrayList al){
        finalScores = al;
    }
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public ArrayList getScores(){
        return scores;
    }
    public void setFinalScores(double[] d){
        if(d.length != scores.size()){
            throw new IllegalArgumentException("Length mismatch in setFinalScores!");
        }
        finalScores = new ArrayList();
        for(int i = 0; i<d.length; i++){
            finalScores.add(new Double(d[i]));
        }
    }
    public ArrayList getCoders(){
        return coderIds;
    }
    public ArrayList getFinalScores(){
        return finalScores;
    }
    public void sort(int[] idxs){
        so = idxs;
        Collections.sort(records);
    }
    public ArrayList getRecords(){
        return records;
    }
    public ArrayList getTestCaseIds(){
        return testCaseIds;
    }
    public class Record implements Comparable, Serializable {
        public static final int CODER_SORT = Integer.MAX_VALUE-1;
        public static final int TOTAL_SORT = Integer.MAX_VALUE;
        private double score;
        private int coderID;
        private String handle;
        private ArrayList tests;
        public Record(int coderID, String handle, double score, ArrayList tests){
            this.coderID = coderID;
            this.handle = handle;
            this.score = score;
            this.tests = tests;
        }
        public double getScore(){return score;}
        public int getCoderID(){return coderID;}
        public String getHandle(){return handle;}
        public ArrayList getTests(){return tests;}
        public LongRoundResults getParent(){return LongRoundResults.this;}
        public double getTestScore(int idx){
            return ((Double)tests.get(idx)).doubleValue();
        }
        public int compareTo(Object o){
            Record r = (Record)o;
            if(so == null)return 0;
            for(int i = 0; i<so.length; i++){
                int abs = Math.abs(so[i])-1;
                if(abs == CODER_SORT){
                    return (so[i]>0?1:-1)*handle.compareTo(r.handle);
                }else if(abs == TOTAL_SORT){
                    if(score != r.score){
                        return (so[i]>0?-1:1)*(score - r.score) > 0 ? 1 : -1;
                    }
                }else{
                    double t1 = getTestScore(abs);
                    double t2 = r.getTestScore(abs);
                    if(t1 != t2){
                        return (so[i]>0?-1:1)*(t1-t2) > 0 ? 1 : -1;
                    }
                }
            }
            return 0;
        }
    }
}
