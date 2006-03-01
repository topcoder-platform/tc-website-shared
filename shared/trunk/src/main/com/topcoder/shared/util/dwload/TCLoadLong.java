package com.topcoder.shared.util.dwload;

import com.topcoder.shared.util.DBMS;
import com.topcoder.shared.util.logging.Logger;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Arrays;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * @author dok
 * @version $Revision$ $Date$
 *          Create Date: Oct 31, 2005
 */
public class TCLoadLong extends TCLoad {
    private static Logger log = Logger.getLogger(TCLoadRound.class);
    protected java.sql.Timestamp fStartTime = null;
    protected java.sql.Timestamp fLastLogTime = null;

    // The following set of variables are all configureable from the command
    // line by specifying -variable (where the variable is after the //)
    // followed by the new value
    private int fRoundId = -1;                 // roundid
    private int CODING_SEGMENT_ID = 2;    // codingseg
    private int CONTEST_ROOM = 2;    // contestroom
    private int ROUND_LOG_TYPE = 1;    // roundlogtype
    private boolean FULL_LOAD = false;//fullload


    /**
     * This method is passed any parameters passed to this load
     */
    public boolean setParameters(Hashtable params) {
        try {
            Integer tmp;
            Boolean tmpBool;
            fRoundId = retrieveIntParam("roundid", params, false, true).intValue();

            tmp = retrieveIntParam("codingseg", params, true, true);
            if (tmp != null) {
                CODING_SEGMENT_ID = tmp.intValue();
                log.info("New coding segment id is " + CODING_SEGMENT_ID);
            }


            tmp = retrieveIntParam("contestroom", params, true, true);
            if (tmp != null) {
                CONTEST_ROOM = tmp.intValue();
                log.info("New contestroom id is " + CONTEST_ROOM);
            }

            tmp = retrieveIntParam("roundlogtype", params, true, true);
            if (tmp != null) {
                ROUND_LOG_TYPE = tmp.intValue();
                log.info("New roundlogtype is " + ROUND_LOG_TYPE);
            }

            tmpBool = retrieveBooleanParam("fullload", params, true);
            if (tmpBool != null) {
                FULL_LOAD = tmpBool.booleanValue();
                log.info("New fullload flag is " + FULL_LOAD);
            }


        } catch (Exception ex) {
            setReasonFailed(ex.getMessage());
            return false;
        }

        return true;
    }

    /**
     * This method performs the load for the round information tables
     */
    public void performLoad() throws Exception {
        try {
            log.info("Loading round: " + fRoundId);

            fStartTime = new java.sql.Timestamp(System.currentTimeMillis());

            getLastUpdateTime();

            clearRound();

            loadContest();

            loadRound();

            loadProblem();

            loadProblemCategory();

            loadProblemSubmission();

            loadSystemTestCase();

            loadSystemTestResult();

            loadResult();

            setLastUpdateTime();

            log.info("SUCCESS: Round " + fRoundId +
                    " load ran successfully.");
        } catch (Exception ex) {
            setReasonFailed(ex.getMessage());
            throw ex;
        }
    }

    private void clearRound() throws Exception {
        PreparedStatement ps = null;
        ArrayList a = null;

        try {
            a = new ArrayList();


            a.add(new String("DELETE FROM system_test_case WHERE problem_id in (SELECT problem_id FROM round_problem WHERE round_id = ?)"));
            a.add(new String("DELETE FROM long_system_test_result WHERE round_id = ?"));
            a.add(new String("DELETE FROM long_problem_submission WHERE round_id = ?"));
            a.add(new String("DELETE FROM problem_category_xref where problem_id in (select problem_id from problem where round_id = ?)"));
            a.add(new String("DELETE FROM problem WHERE round_id = ?"));
            a.add(new String("DELETE FROM long_comp_result WHERE round_id = ?"));

            int count = 0;
            for (int i = 0; i < a.size(); i++) {
                ps = prepareStatement((String) a.get(i), TARGET_DB);
                if (((String) a.get(i)).indexOf('?') > -1)
                    ps.setInt(1, fRoundId);
                count = ps.executeUpdate();
                log.info("" + count + " rows: " + (String) a.get(i));
            }
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("clearing data failed.\n" +
                    sqle.getMessage());
        } finally {
            close(ps);
        }
    }


    private void getLastUpdateTime() throws Exception {
        Statement stmt = null;
        ResultSet rs = null;
        StringBuffer query = null;

        try {
            query = new StringBuffer(100);
            query.append("select timestamp from update_log where log_id = ");
            query.append("(select max(log_id) from update_log where log_type_id = " + ROUND_LOG_TYPE + ")");
            stmt = createStatement(TARGET_DB);
            rs = stmt.executeQuery(query.toString());
            if (rs.next()) {
                fLastLogTime = rs.getTimestamp(1);
            } else {
                throw new SQLException("Last log time not found in update_log table");
            }
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Failed to retrieve last log time.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(stmt);
        }
    }

    /**
     * This method loads the 'problem_submission' table which holds
     * information for a given round and given coder, the results of a
     * particular problem
     */
    private void loadProblemSubmission() throws Exception {
        int retVal = 0;
        int count = 0;
        PreparedStatement psSel = null;
        PreparedStatement psIns = null;
        PreparedStatement psDel = null;
        ResultSet rs = null;
        StringBuffer query = null;

        try {
            query = new StringBuffer(100);
            query.append(" SELECT cs.round_id");              //1
            query.append(" ,cs.coder_id ");            //2
            query.append(" , (SELECT cm.problem_id FROM component cm WHERE cm.component_id = cs.component_id)");          //3
            query.append(" ,cs.points ");              //4
            query.append(" ,cs.status_id ");           //5
            query.append(" ,CASE WHEN s.language_id is null THEN c.language_id ELSE s.language_id END as language_id");         //6
            query.append(" ,s.open_time ");            //7
            query.append(" ,cs.submission_number ");   //8
            query.append(" ,s.submission_text ");      //9
            query.append(" ,s.submit_time ");          //10
            query.append(" ,s.submission_points ");    //11
            query.append("  ,(SELECT status_desc ");   //12
            query.append(" FROM problem_status_lu ");
            query.append(" WHERE problem_status_id = cs.status_id) ");
            query.append(" ,c.compilation_text");      //13
            query.append(" ,s.submission_number");     //14
            query.append(" FROM long_component_state cs ");
            query.append("     , outer long_submission s");
            query.append("     , outer long_compilation");
            query.append(" WHERE cs.round_id = ?");
            query.append("  and cs.long_component_state_id = c.long_component_state_id");
            query.append("   and cs.long_component_state_id = s.long_component_state_id");
            query.append("   AND NOT EXISTS ");
            query.append("       (SELECT 'pops' ");
            query.append("          FROM group_user gu ");
            query.append("         WHERE gu.user_id = cs.coder_id ");
            query.append("           AND gu.group_id in (13,14))");

            psSel = prepareStatement(query.toString(), SOURCE_DB);

            query = new StringBuffer(100);
            query.append("INSERT INTO problem_submission ");
            query.append("      (round_id ");            // 1
            query.append("       ,coder_id ");           // 2
            query.append("       ,problem_id ");         // 3
            query.append("       ,final_points ");       // 4
            query.append("       ,status_id ");          // 5
            query.append("       ,language_id ");        // 6
            query.append("       ,open_time ");          // 7
            query.append("       ,submission_number ");  // 8
            query.append("       ,submission_text ");    // 9
            query.append("       ,submit_time ");        // 10
            query.append("       ,submission_points ");  // 11
            query.append("       ,status_desc ");        // 12
            query.append("       ,last_submission ");   // 13
            query.append("       ,example) ");   // 14
            query.append("VALUES (");
            query.append("?,?,?,?,?,?,?,?,?,?,");  // 10
            query.append("?,?,?,?)");                // 14 total values
            psIns = prepareStatement(query.toString(), TARGET_DB);

            query = new StringBuffer(100);
            query.append("DELETE FROM problem_submission ");
            query.append(" WHERE round_id = ? ");
            query.append("   AND coder_id = ? ");
            query.append("   AND problem_id = ?");
            query.append("   AND submission_number = ?");
            query.append("   and example = ?");
            psDel = prepareStatement(query.toString(), TARGET_DB);

            // On to the load
            psSel.setInt(1, fRoundId);
            rs = psSel.executeQuery();

            while (rs.next()) {
                long round_id = rs.getInt(1);
                long coder_id = rs.getInt(2);
                long problem_id = rs.getInt(3);
                int submission_number = rs.getInt(14);
                int example = rs.getInt("example");
                int last_submission = 0;
                if (rs.getInt(8) > 0) {  //they submitted at least once
                    last_submission = rs.getInt(8) == submission_number ? 1 : 0;
                }

                psDel.clearParameters();
                psDel.setLong(1, round_id);
                psDel.setLong(2, coder_id);
                psDel.setLong(3, problem_id);
                psDel.setInt(4, submission_number);
                psDel.setInt(5, example);
                psDel.executeUpdate();

                psIns.clearParameters();
                psIns.setLong(1, round_id);  // round_id
                psIns.setLong(2, coder_id);  // coder_id
                psIns.setLong(3, problem_id);  // problem_id
                psIns.setFloat(4, rs.getFloat("points"));  // final_points
                psIns.setInt(5, rs.getInt("status_id"));  // status_id
                psIns.setInt(6, rs.getInt("language_id"));  // language_id
                psIns.setLong(7, rs.getLong("open_time"));  // open_time
                psIns.setInt(8, rs.getInt(14));  // submission_number
                if (Arrays.equals(getBytes(rs, 9), "".getBytes()))
                    setBytes(psIns, 9, getBytes(rs, 13));       // use compilation_text
                else
                    setBytes(psIns, 9, getBytes(rs, 9));       // use submission_text
                psIns.setLong(10, rs.getLong(10));  // submit_time
                psIns.setFloat(11, rs.getFloat(11));  // submission_points
                psIns.setString(12, rs.getString(12));  // status_desc
                psIns.setInt(13, last_submission);  // last_submission
                psIns.setInt(14, example);

                retVal = psIns.executeUpdate();
                count += retVal;
                if (retVal != 1) {
                    throw new SQLException("TCLoadRound: Insert for coder_id " +
                            coder_id + ", round_id " + round_id +
                            ", problem_id " + problem_id +
                            " modified " + retVal + " rows, not one.");
                }

                printLoadProgress(count, "problem_submission");
            }

            log.info("Problem_submission records copied = " + count);
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'problem_submission' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(psSel);
            close(psIns);
            close(psDel);
        }
    }

    /**
     * This load the 'system_test_case' table
     */
    private void loadSystemTestCase() throws Exception {
        int retVal = 0;
        int count = 0;
        PreparedStatement psSel = null;
        PreparedStatement psIns = null;
        PreparedStatement psDel = null;
        ResultSet rs = null;
        StringBuffer query = null;

        try {
            query = new StringBuffer(100);
            query.append("SELECT stc.test_case_id ");      // 1
            query.append("       ,comp.problem_id ");       // 2
            query.append("       ,stc.args ");             // 3
            query.append("       ,stc.expected_result ");  // 4
            query.append("       ,CURRENT ");              // 5
            query.append("       ,stc.example_flag ");
            query.append("  FROM system_test_case stc, component comp ");
            query.append(" WHERE comp.component_id in (SELECT component_id FROM round_component WHERE round_id = ?)");
            query.append(" AND comp.component_id = stc.component_id");
            psSel = prepareStatement(query.toString(), SOURCE_DB);

            query = new StringBuffer(100);
            query.append("INSERT INTO system_test_case ");
            query.append("      (test_case_id ");      // 1
            query.append("       ,problem_id ");       // 2
            query.append("       ,args ");             // 3
            query.append("       ,expected_result ");  // 4
            query.append("       ,modify_date ");     // 5
            query.append("       ,example_flag)");    //6
            query.append("VALUES ( ");
            query.append("?,?,?,?,?,?)");  // 6 total values
            psIns = prepareStatement(query.toString(), TARGET_DB);

            query = new StringBuffer(100);
            query.append("DELETE FROM system_test_case ");
            query.append(" WHERE test_case_id = ? ");
            query.append("   AND problem_id = ?");
            psDel = prepareStatement(query.toString(), TARGET_DB);

            // On to the load
            psSel.setInt(1, fRoundId);
            rs = psSel.executeQuery();

            while (rs.next()) {
                int test_case_id = rs.getInt(1);
                int problem_id = rs.getInt(2);

                psDel.clearParameters();
                psDel.setInt(1, test_case_id);
                psDel.setInt(2, problem_id);
                psDel.executeUpdate();

                psIns.clearParameters();
                psIns.setInt(1, rs.getInt(1));  // test_case_id
                psIns.setInt(2, rs.getInt(2));  // problem_id
                setBytes(psIns, 3, getBlobObject(rs, 3));  // args
                setBytes(psIns, 4, getBlobObject(rs, 4));  // expected_result
                psIns.setTimestamp(5, rs.getTimestamp(5));  // modify_date
                psIns.setInt(6, rs.getInt("example_flag"));

                retVal = psIns.executeUpdate();
                count += retVal;
                if (retVal != 1) {
                    throw new SQLException("TCLoadRound: Insert for test_case_id " +
                            test_case_id + ", problem_id " + problem_id +
                            " modified more than one row.");
                }

                printLoadProgress(count, "system_test_case");
            }

            log.info("System_test_case records copied = " + count);
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'system_test_case' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(psSel);
            close(psIns);
            close(psDel);
        }
    }

    /**
     * This load the 'system_test_result' table which holds the results
     * of the system tests for a give round, coder and problem.
     */
    private void loadSystemTestResult() throws Exception {
        int retVal = 0;
        int count = 0;
        PreparedStatement psSel = null;
        PreparedStatement psIns = null;
        PreparedStatement psDel = null;
        ResultSet rs = null;
        StringBuffer query = null;

        try {
            query = new StringBuffer(100);
            query.append("SELECT str.coder_id ");
            query.append(",str.round_id ");
            query.append(",comp.problem_id ");
            query.append(",str.test_case_id ");
            query.append(",str.submission_number ");
            query.append(",str.example ");
            query.append(",str.processing_time ");
            query.append(",str.timestamp ");
            query.append(",str.fatal_errors ");
            query.append(",str.score ");
            query.append("FROM long_system_test_result str, component comp ");
            query.append("WHERE str.round_id = ? ");
            query.append("AND comp.component_id = str.component_id ");
            query.append("AND str.coder_id NOT IN ");
            query.append("(SELECT gu.user_id ");
            query.append("FROM group_user gu ");
            query.append("WHERE gu.group_id IN (13,14)) ");


            psSel = prepareStatement(query.toString(), SOURCE_DB);

            query = new StringBuffer(100);
            query.append("INSERT INTO long_system_test_result ");
            query.append("      (coder_id ");
            query.append("       ,round_id ");
            query.append("       ,problem_id ");
            query.append("       ,test_case_id ");
            query.append("       ,submission_number");
            query.append("       ,example ");
            query.append("       ,processing_time ");
            query.append("       ,timestamp ");
            query.append("       ,fatal_errors ");
            query.append("       ,score)");
            query.append("VALUES (");
            query.append("?,?,?,?,?,?,?,?,?,?)");  // 10 values
            psIns = prepareStatement(query.toString(), TARGET_DB);

            query = new StringBuffer(100);
            query.append("DELETE FROM long_system_test_result ");
            query.append(" WHERE coder_id = ? ");
            query.append("   AND round_id = ? ");
            query.append("   AND problem_id = ? ");
            query.append("   AND test_case_id = ?");
            query.append("   and submission_number = ?");
            query.append("   and example = ?");
            psDel = prepareStatement(query.toString(), TARGET_DB);

            // On to the load
            psSel.setInt(1, fRoundId);
            rs = psSel.executeQuery();

            while (rs.next()) {
                long coder_id = rs.getLong("coder_id");
                long round_id = rs.getLong("round_id");
                long problem_id = rs.getLong("problem_id");
                long test_case_id = rs.getLong("test_case_id");
                int subNum = rs.getInt("submission_number");
                int example = rs.getInt("example");


                psDel.clearParameters();
                psDel.setLong(1, coder_id);
                psDel.setLong(2, round_id);
                psDel.setLong(3, problem_id);
                psDel.setLong(4, test_case_id);
                psDel.setInt(5, subNum);
                psDel.setInt(6, example);
                psDel.executeUpdate();

                psIns.clearParameters();
                psIns.setLong(1, coder_id);
                psIns.setLong(2, round_id);
                psIns.setLong(3, problem_id);
                psIns.setLong(4, test_case_id);
                psIns.setInt(5, subNum);
                psIns.setLong(6, example);
                psIns.setFloat(7, rs.getLong("processing_time"));
                psIns.setTimestamp(8, rs.getTimestamp("timestamp"));
                psIns.setBytes(9, rs.getBytes("fatal_errors"));
                psIns.setFloat(10, rs.getFloat("score"));

                retVal = psIns.executeUpdate();
                count += retVal;
                if (retVal != 1) {
                    throw new SQLException("TCLoadRound: Insert for coder_id " +
                            coder_id + ", round_id " + round_id +
                            ", problem_id " + problem_id +
                            ", test_case_id " + test_case_id + " subnum " + subNum + " example: " + example +
                            " modified " + retVal + " rows, not one.");
                }

                printLoadProgress(count, "long_system_test_result");
            }

            log.info("long_system_test_result records copied = " + count);
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'long_system_test_result' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(psSel);
            close(psIns);
            close(psDel);
        }
    }

    /**
     * This loads the 'contest' table
     */
    private void loadContest() throws Exception {
        int retVal = 0;
        int count = 0;
        PreparedStatement psSel = null;
        PreparedStatement psIns = null;
        PreparedStatement psSel2 = null;
        PreparedStatement psUpd = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        StringBuffer query = null;

        try {
            query = new StringBuffer(100);
            query.append("SELECT c.contest_id ");    // 1
            query.append("       ,c.name ");         // 2
            query.append("       ,c.start_date ");   // 3
            query.append("       ,c.end_date ");     // 4
            query.append("       ,c.status ");       // 5
            query.append("       ,c.group_id ");     // 6
            query.append("       ,c.region_code ");  // 7
            query.append("       ,c.ad_text ");      // 8
            query.append("       ,c.ad_start ");     // 9
            query.append("       ,c.ad_end ");       // 10
            query.append("       ,c.ad_task ");      // 11
            query.append("       ,c.ad_command ");   // 12
            query.append("  FROM contest c ");
            query.append("       ,round r ");
            query.append(" WHERE r.round_id = ? ");
            query.append("   AND r.contest_id = c.contest_id");
            psSel = prepareStatement(query.toString(), SOURCE_DB);

            query = new StringBuffer(100);
            query.append("INSERT INTO contest ");
            query.append("      (contest_id ");    // 1
            query.append("       ,name ");         // 2
            query.append("       ,start_date ");   // 3
            query.append("       ,end_date ");     // 4
            query.append("       ,status ");       // 5
            query.append("       ,group_id ");     // 6
            query.append("       ,region_code ");  // 7
            query.append("       ,ad_text ");      // 8
            query.append("       ,ad_start ");     // 9
            query.append("       ,ad_end ");       // 10
            query.append("       ,ad_task ");      // 11
            query.append("       ,ad_command) ");  // 12
            query.append("VALUES (");
            query.append("?,?,?,?,?,?,?,?,?,?,");  // 10 values
            query.append("?,?)");                  // 12 total values
            psIns = prepareStatement(query.toString(), TARGET_DB);

            query = new StringBuffer(100);
            query.append("UPDATE contest ");
            query.append("   SET name = ? ");          // 1
            query.append("       ,start_date = ? ");   // 2
            query.append("       ,end_date = ? ");     // 3
            query.append("       ,status = ? ");       // 4
            query.append("       ,group_id = ? ");     // 5
            query.append("       ,region_code = ? ");  // 6
            query.append("       ,ad_text = ? ");      // 7
            query.append("       ,ad_start = ? ");     // 8
            query.append("       ,ad_end = ? ");       // 9
            query.append("       ,ad_task = ? ");      // 10
            query.append("       ,ad_command = ? ");  // 11
            query.append(" WHERE contest_id = ? ");    // 12
            psUpd = prepareStatement(query.toString(), TARGET_DB);

            query = new StringBuffer(100);
            query.append("SELECT 'pops' ");
            query.append("  FROM contest ");
            query.append(" WHERE contest_id = ?");
            psSel2 = prepareStatement(query.toString(), TARGET_DB);

            // On to the load
            psSel.setInt(1, fRoundId);
            rs = psSel.executeQuery();

            while (rs.next()) {
                int contest_id = rs.getInt(1);
                psSel2.clearParameters();
                psSel2.setInt(1, contest_id);
                rs2 = psSel2.executeQuery();

                // If next() returns true that means this row exists. If so,
                // we update. Otherwise, we insert.
                if (rs2.next()) {
                    psUpd.clearParameters();
                    psUpd.setString(1, rs.getString(2));  // name
                    psUpd.setTimestamp(2, rs.getTimestamp(3));  // start_date
                    psUpd.setTimestamp(3, rs.getTimestamp(4));  // end_date
                    psUpd.setString(4, rs.getString(5));  // status
                    psUpd.setInt(5, rs.getInt(6));  // group_id
                    psUpd.setString(6, rs.getString(7));  // region_code
                    psUpd.setString(7, rs.getString(8));  // ad_text
                    psUpd.setTimestamp(8, rs.getTimestamp(9));  // ad_start
                    psUpd.setTimestamp(9, rs.getTimestamp(10));  // ad_end
                    psUpd.setString(10, rs.getString(11));  // ad_task
                    psUpd.setString(11, rs.getString(12));  // ad_command
                    psUpd.setInt(12, rs.getInt(1));  // contest_id

                    retVal = psUpd.executeUpdate();
                    count += retVal;
                    if (retVal != 1) {
                        throw new SQLException("TCLoadRound: Insert for contest_id " +
                                contest_id +
                                " modified " + retVal + " rows, not one.");
                    }
                } else {
                    psIns.clearParameters();
                    psIns.setInt(1, rs.getInt(1));  // contest_id
                    psIns.setString(2, rs.getString(2));  // name
                    psIns.setTimestamp(3, rs.getTimestamp(3));  // start_date
                    psIns.setTimestamp(4, rs.getTimestamp(4));  // end_date
                    psIns.setString(5, rs.getString(5));  // status
                    psIns.setInt(6, rs.getInt(6));  // group_id
                    psIns.setString(7, rs.getString(7));  // region_code
                    psIns.setString(8, rs.getString(8));  // ad_text
                    psIns.setTimestamp(9, rs.getTimestamp(9));  // ad_start
                    psIns.setTimestamp(10, rs.getTimestamp(10));  // ad_end
                    psIns.setString(11, rs.getString(11));  // ad_task
                    psIns.setString(12, rs.getString(12));  // ad_command

                    retVal = psIns.executeUpdate();
                    count += retVal;
                    if (retVal != 1) {
                        throw new SQLException("TCLoadRound: Insert for contest_id " +
                                contest_id +
                                " modified " + retVal + " rows, not one.");
                    }
                }

                close(rs2);
                printLoadProgress(count, "contest");
            }

            log.info("Contest records copied = " + count);
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'contest' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(rs2);
            close(psSel);
            close(psSel2);
            close(psIns);
            close(psUpd);
        }
    }

    /**
     * This loads the 'problem' table
     */
    private void loadProblem() throws Exception {
        int retVal = 0;
        int count = 0;
        PreparedStatement psSel = null;
        PreparedStatement psSel2 = null;
        PreparedStatement psIns = null;
        PreparedStatement psUpd = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        StringBuffer query = null;

        try {
            query = new StringBuffer(100);
            query.append("SELECT p.problem_id ");                             // 1
            query.append("       ,rp.round_id ");                             // 2
            query.append("       ,c.result_type_id ");                        // 3
            query.append("       ,c.method_name ");                           // 4
            query.append("       ,c.class_name ");                            // 5
            query.append("       ,p.status_id ");                                // 6
            query.append("       ,c.default_solution ");                      // 7
            query.append("       ,c.component_text ");                          // 8
            query.append("       ,CURRENT ");                                 // 9
            query.append("       ,(SELECT data_type_desc ");                  // 10
            query.append("           FROM data_type ");
            query.append("          WHERE data_type_id = c.result_type_id) ");
            query.append("       ,d.difficulty_id ");                         // 11
            query.append("       ,d.difficulty_desc ");                       // 12
            query.append("       ,rp.division_id ");                          // 13
            query.append("       ,rp.points ");                               // 14
            query.append("  FROM problem p ");
            query.append("       ,round_component rp ");
            query.append("       ,difficulty d ");
            query.append("       ,component c ");
            query.append(" WHERE rp.round_id = ? ");
            query.append("   AND p.problem_id = c.problem_id");
            query.append("   AND c.component_id = rp.component_id ");
            query.append("   AND rp.difficulty_id = d.difficulty_id");
            psSel = prepareStatement(query.toString(), SOURCE_DB);

            query = new StringBuffer(100);
            query.append("INSERT INTO problem ");
            query.append("      (problem_id ");         // 1
            query.append("       ,round_id ");          // 2
            query.append("       ,result_type_id ");    // 3
            query.append("       ,method_name ");       // 4
            query.append("       ,class_name ");        // 5
            query.append("       ,status ");            // 6
            query.append("       ,default_solution ");  // 7
            query.append("       ,problem_text ");      // 8
            query.append("       ,modify_date ");       // 9
            query.append("       ,result_type_desc ");  // 10
            query.append("       ,level_id ");          // 11
            query.append("       ,level_desc ");        // 12
            query.append("       ,division_id ");       // 13
            query.append("       ,points ");            // 14
            query.append("       ,viewable) ");         // 15
            query.append("VALUES (");
            query.append("?,?,?,?,?,?,?,?,?,?,");
            query.append("?,?,?,?,?)");
            psIns = prepareStatement(query.toString(), TARGET_DB);

            query = new StringBuffer(100);
            query.append("UPDATE problem ");
            query.append("   SET result_type_id = ? ");     // 1
            query.append("       ,method_name = ? ");       // 2
            query.append("       ,class_name = ? ");        // 3
            query.append("       ,status = ? ");            // 4
            query.append("       ,default_solution = ? ");  // 5
            query.append("       ,problem_text = ? ");      // 6
            query.append("       ,modify_date = ? ");       // 7
            query.append("       ,result_type_desc = ? ");  // 8
            query.append("       ,level_id = ? ");          // 9
            query.append("       ,level_desc = ? ");        // 10
            query.append("       ,points = ? ");            // 11
            query.append("       ,viewable = ?");           // 12
            query.append(" WHERE problem_id = ? ");         // 13
            query.append("   AND round_id = ? ");           // 14
            query.append("   AND division_id = ? ");        // 15
            psUpd = prepareStatement(query.toString(), TARGET_DB);

            query = new StringBuffer(100);
            query.append("SELECT 'pops' FROM problem ");
            query.append(" WHERE problem_id = ? ");
            query.append("   AND round_id = ?");
            query.append("   AND division_id = ?");
            psSel2 = prepareStatement(query.toString(), TARGET_DB);

            // On to the load
            psSel.setInt(1, fRoundId);
            rs = psSel.executeQuery();

            while (rs.next()) {
                int problem_id = rs.getInt(1);
                int round_id = rs.getInt(2);
                int division_id = rs.getInt(13);

                psSel2.clearParameters();
                psSel2.setInt(1, problem_id);
                psSel2.setInt(2, round_id);
                psSel2.setInt(3, division_id);
                rs2 = psSel2.executeQuery();

                // If next() returns true that means this row exists. If so,
                // we update. Otherwise, we insert.
                if (rs2.next()) {
                    psUpd.clearParameters();
                    psUpd.setInt(1, rs.getInt(3));  // result_type_id
                    psUpd.setString(2, rs.getString(4));  // method_name
                    psUpd.setString(3, rs.getString(5));  // class_name
                    psUpd.setInt(4, rs.getInt(6));  // status
                    setBytes(psUpd, 5, getBytes(rs, 7));  // default_solution
                    setBytes(psUpd, 6, getBytes(rs, 8));  // problem_text
                    psUpd.setTimestamp(7, rs.getTimestamp(9));  // modify_date
                    psUpd.setString(8, rs.getString(10));  // result_type_desc
                    psUpd.setInt(9, rs.getInt(11));  // level_id
                    psUpd.setString(10, rs.getString(12));  // level_desc
                    psUpd.setFloat(11, rs.getFloat(14)); // points
                    psUpd.setInt(12, 1); //viewable
                    psUpd.setInt(13, rs.getInt(1));  // problem_id
                    psUpd.setInt(14, rs.getInt(2));  // round_id
                    psUpd.setInt(15, rs.getInt(13));  // division_id

                    retVal = psUpd.executeUpdate();
                    count += retVal;
                    if (retVal != 1) {
                        throw new SQLException("TCLoadRound: Update for problem_id " +
                                problem_id + ", round_id " + round_id +
                                " modified " + retVal + " rows, not one.");
                    }
                } else {
                    psIns.clearParameters();
                    psIns.setInt(1, rs.getInt(1));  // problem_id
                    psIns.setInt(2, rs.getInt(2));  // round_id
                    psIns.setInt(3, rs.getInt(3));  // result_type_id
                    psIns.setString(4, rs.getString(4));  // method_name
                    psIns.setString(5, rs.getString(5));  // class_name
                    psIns.setInt(6, rs.getInt(6));  // status
                    setBytes(psIns, 7, getBytes(rs, 7));  // default_solution
                    setBytes(psIns, 8, getBytes(rs, 8));  // problem_text
                    psIns.setTimestamp(9, rs.getTimestamp(9));  // modify_date
                    psIns.setString(10, rs.getString(10));  // result_type_desc
                    psIns.setInt(11, rs.getInt(11));  // level_id
                    psIns.setString(12, rs.getString(12));  // level_desc
                    psIns.setInt(13, rs.getInt(13));  // division_id
                    psIns.setFloat(14, rs.getFloat(14)); //points
                    psIns.setInt(15, 1); //viewable

                    retVal = psIns.executeUpdate();
                    count += retVal;
                    if (retVal != 1) {
                        throw new SQLException("TCLoadRound: Insert for problem_id " +
                                problem_id + ", round_id " + round_id +
                                " modified " + retVal + " rows, not one.");
                    }
                }

                close(rs2);
                printLoadProgress(count, "problem");
            }

            log.info("Problem records copied = " + count);
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'problem' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(rs2);
            close(psSel);
            close(psSel2);
            close(psIns);
            close(psUpd);
        }
    }

    /**
     * This loads the 'round' table
     */
    private void loadRound() throws Exception {
        int retVal = 0;
        int count = 0;
        PreparedStatement psSel = null;
        PreparedStatement psSel2 = null;
        PreparedStatement psIns = null;
        PreparedStatement psUpd = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        StringBuffer query = null;

        try {
            query = new StringBuffer(100);
            query.append("SELECT r.round_id ");                          // 1
            query.append("       ,r.contest_id ");                       // 2
            query.append("       ,r.name ");                             // 3
            query.append("       ,r.status ");                           // 4
            query.append("       ,rs.start_time ");                      // 5
            query.append("       ,r.round_type_id ");                    // 6
            query.append("       ,r.invitational ");                     // 7
            query.append("       ,r.notes ");                            // 8
            query.append("       ,(SELECT rtlu.round_type_desc ");       // 9
            query.append("           FROM round_type_lu rtlu ");
            query.append("          WHERE rtlu.round_type_id = r.round_type_id) ");
            query.append("       ,r.short_name ");                       // 10
            query.append("       ,r.forum_id");                          // 11
            query.append("  FROM round r ");
            query.append("       ,round_segment rs ");
            query.append(" WHERE r.round_id = ? ");
            query.append("   AND rs.round_id = r.round_id ");
            query.append("   AND rs.segment_id = " + CODING_SEGMENT_ID);
            psSel = prepareStatement(query.toString(), SOURCE_DB);

            // We have 8 values in the insert as opposed to 7 in the select
            // because we want to provide a default value for failed. We
            // don't have a place to select failed from in the transactional
            // DB
            query = new StringBuffer(100);
            query.append("INSERT INTO round ");
            query.append("      (round_id ");          // 1
            query.append("       ,contest_id ");       // 2
            query.append("       ,name ");             // 3
            query.append("       ,status ");           // 4
            query.append("       ,calendar_id ");      // 5
            query.append("       ,failed ");           // 6
            query.append("       ,round_type_id ");    // 7
            query.append("       ,invitational  ");    // 8
            query.append("       ,notes         ");    // 9
            query.append("       ,round_type_desc ");  // 10
            query.append("       ,short_name ");       // 11
            query.append("       ,forum_id)");         // 12
            query.append("VALUES (");
            query.append("?,?,?,?,?,?,?,?,?,");
            query.append("?,?,?)");

            psIns = prepareStatement(query.toString(), TARGET_DB);

            query = new StringBuffer(100);
            query.append("UPDATE round ");
            query.append("   SET contest_id = ? ");       // 1
            query.append("       ,name = ? ");            // 2
            query.append("       ,status = ? ");          // 3
            query.append("       ,calendar_id = ? ");     // 4
            query.append("       ,failed = ? ");          // 5
            query.append("       ,round_type_id = ? ");   // 6
            query.append("       ,invitational  = ? ");   // 7
            query.append("       ,notes = ?         ");   // 8
            query.append("       ,round_type_desc = ? "); // 9
            query.append("       ,short_name = ? ");      // 10
            query.append("       ,forum_id = ? ");        // 11
            query.append(" WHERE round_id = ? ");         // 12
            psUpd = prepareStatement(query.toString(), TARGET_DB);

            query = new StringBuffer(100);
            query.append("SELECT 'pops' FROM round where round_id = ?");
            psSel2 = prepareStatement(query.toString(), TARGET_DB);

            // On to the load
            psSel.setInt(1, fRoundId);
            rs = psSel.executeQuery();
            while (rs.next()) {
                int round_id = rs.getInt("round_id");
                psSel2.clearParameters();
                psSel2.setInt(1, round_id);
                rs2 = psSel2.executeQuery();

                // Retrieve the calendar_id for the start_time of this round
                java.sql.Timestamp stamp = rs.getTimestamp("start_time");
                int calendar_id = lookupCalendarId(stamp, TARGET_DB);

                // If next() returns true that means this row exists. If so,
                // we update. Otherwise, we insert.
                if (rs2.next()) {
                    psUpd.clearParameters();
                    psUpd.setInt(1, rs.getInt("contest_id"));  // contest_id
                    psUpd.setString(2, rs.getString("name"));  // name
                    psUpd.setString(3, rs.getString("status"));  // status
                    psUpd.setInt(4, calendar_id);         // cal_id of start_time
                    psUpd.setInt(5, 0);                   // failed (default is 0)
                    psUpd.setInt(6, rs.getInt("round_type_id"));        // round_type_id
                    psUpd.setInt(7, rs.getInt("invitational"));        // invitational
                    psUpd.setString(8, rs.getString("notes"));     // notes
                    psUpd.setString(9, rs.getString(10));    // round_type_desc
                    psUpd.setString(10, rs.getString("short_name"));   // shortname
                    psUpd.setInt(11, rs.getInt("forum_id"));   // forum_id
                    psUpd.setInt(12, rs.getInt(1));  // round_id

                    retVal = psUpd.executeUpdate();
                    count += retVal;
                    if (retVal != 1) {
                        throw new SQLException("TCLoadRound: Update for round_id " +
                                round_id +
                                " modified " + retVal + " rows, not one.");
                    }
                } else {
                    psIns.clearParameters();
                    psIns.setInt(1, round_id);  // round_id
                    psIns.setInt(2, rs.getInt("contest_id"));  // contest_id
                    psIns.setString(3, rs.getString("name"));  // name
                    psIns.setString(4, rs.getString("status"));  // status
                    psIns.setInt(5, calendar_id);  // cal_id of start_time
                    psIns.setInt(6, 0);                   // failed (default is 0)
                    psIns.setInt(7, rs.getInt("round_type_id"));        // round_type_id
                    psIns.setInt(8, rs.getInt("invitational"));        // invitational
                    psIns.setString(9, rs.getString("notes"));     // notes
                    psIns.setString(10, rs.getString(10));    // round_type_desc
                    psIns.setString(11, rs.getString("short_name"));  // short name
                    psIns.setString(12, rs.getString("forum_id"));  // forum_id

                    retVal = psIns.executeUpdate();
                    count += retVal;
                    if (retVal != 1) {
                        throw new SQLException("TCLoadRound: Insert for round_id " +
                                round_id +
                                " modified " + retVal + " rows, not one.");
                    }
                }

                close(rs2);
                printLoadProgress(count, "round");
            }

            log.info("Round records copied = " + count);
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'round' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rs2);
            close(psSel);
            close(psSel2);
            close(psIns);
            close(psUpd);
        }
    }

    /**
     * This loads the 'room' table
     */

    /**
     * This loads the 'long_comp_result' table.
     */

    private void loadResult() throws Exception {
        int retVal = 0;
        int count = 0;
        PreparedStatement psSel = null;
        PreparedStatement psIns = null;
        PreparedStatement psDel = null;
        ResultSet rs = null;
        StringBuffer query = null;
        long round_id = 0;
        long coder_id = 0;


        try {
            query = new StringBuffer(100);

            query.append("select rr.coder_id ");
            query.append("     , rr.round_id ");
            query.append("     , rr.placed ");
            query.append("     , rr.point_total ");
            query.append("     , cs.submission_number ");
            query.append("     , rr.attended");
            query.append("  from long_room_result rr ");
            query.append("     , long_component_state cs ");
            query.append(" where rr.round_id = ? ");
            query.append("   and cs.round_id = rr.round_id ");
            query.append("   and cs.coder_id = rr.coder_id ");
            query.append("   AND NOT EXISTS ");
            query.append("       (SELECT 'pops' ");
            query.append("          FROM group_user gu ");
            query.append("         WHERE gu.user_id = rr.coder_id ");
            query.append("           AND gu.group_id IN (13,14))");

            psSel = prepareStatement(query.toString(), SOURCE_DB);

            query = new StringBuffer(100);
            query.append("INSERT INTO long_comp_result ");
            query.append("      (round_id ");                         // 1
            query.append("       ,coder_id ");                        // 2
            query.append("       ,placed");                           // 3
            query.append("       ,point_total ");                     // 4
            query.append("       ,num_submissions");                  // 5
            query.append("       ,attended)");                        // 6
            query.append("VALUES (?,?,?,?,?,?)");                     // 6 values
            psIns = prepareStatement(query.toString(), TARGET_DB);

            query = new StringBuffer(100);
            query.append("DELETE FROM long_comp_result ");
            query.append(" WHERE round_id = ? ");
            query.append("   AND coder_id = ? ");
            psDel = prepareStatement(query.toString(), TARGET_DB);

            // On to the load
            psSel.setInt(1, fRoundId);
            rs = psSel.executeQuery();

            while (rs.next()) {
                round_id = rs.getLong("round_id");
                coder_id = rs.getLong("coder_id");

                psDel.clearParameters();
                psDel.setLong(1, round_id);
                psDel.setLong(2, coder_id);
                psDel.executeUpdate();

                psIns.clearParameters();
                psIns.setLong(1, round_id);
                psIns.setLong(2, coder_id);
                psIns.setInt(3, rs.getInt("placed"));
                psIns.setFloat(4, rs.getFloat("point_total"));
                psIns.setInt(5, rs.getInt("submission_number"));
                psIns.setString(6, rs.getString("attended"));

                retVal = psIns.executeUpdate();
                count += retVal;
                if (retVal != 1) {
                    throw new SQLException("TCLoadRound: Insert for coder_id " +
                            coder_id + ", round_id " + round_id +
                            " modified " + retVal + " rows, not one.");
                }

                printLoadProgress(count, "long_comp_result");
            }

            log.info("long_comp_result records copied = " + count);
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'long_comp_result' table failed for coder_id " +
                    coder_id + ", round_id " + round_id +
                    "\n" +
                    sqle.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            close(rs);
            close(psSel);
            close(psIns);
            close(psDel);
        }
    }

    /**
     * This populates the 'challenge' table
     */
    private void loadProblemCategory() throws Exception {
        int retVal = 0;
        int count = 0;
        PreparedStatement psSel = null;
        PreparedStatement psIns = null;
        PreparedStatement psDel = null;
        ResultSet rs = null;
        StringBuffer query = null;

        try {
            query = new StringBuffer(100);
            query.append(" select distinct p.problem_id");
            query.append("      , cc.component_category_id");
            query.append(" from problem p");
            query.append(" , component c");
            query.append(" , component_category_xref cc");
            query.append(" , round_component rc");
            query.append(" where cc.component_id = c.component_id");
            query.append(" and c.problem_id = p.problem_id");
            query.append(" and c.component_id = rc.component_id");
            query.append(" and rc.round_id = ?");

            psSel = prepareStatement(query.toString(), SOURCE_DB);

            query = new StringBuffer(100);
            query.append("INSERT INTO problem_category_xref");
            query.append("      (problem_id ");        // 1
            query.append("       ,problem_category_id) ");       // 2
            query.append("VALUES (");
            query.append("?,?)");  // 2 values
            psIns = prepareStatement(query.toString(), TARGET_DB);

            query = new StringBuffer(100);
            query.append("DELETE FROM problem_category_xref");
            query.append(" WHERE problem_id in ( ");
            query.append(" select problem_id from problem where round_id = ?)");
            psDel = prepareStatement(query.toString(), TARGET_DB);

            // On to the load
            psSel.setInt(1, fRoundId);
            rs = psSel.executeQuery();

            // First thing we do is delete all the challenge entries for this round
            psDel.setInt(1, fRoundId);
            psDel.executeUpdate();

            while (rs.next()) {
                psIns.clearParameters();
                psIns.setLong(1, rs.getLong(1));  // problem_id
                psIns.setLong(2, rs.getLong(2));  // problem_category_id

                retVal = psIns.executeUpdate();
                count += retVal;
                if (retVal != 1) {
                    throw new SQLException("TCLoadRound: Insert for prbolem_id " +
                            rs.getLong(1) + " problem_category_id " + rs.getLong(2) +
                            " modified " + retVal + " rows, not one.");
                }

                printLoadProgress(count, "problem_category_xref");
            }

            log.info("Problem Category records copied = " + count);
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'problem_category_xref' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(psSel);
            close(psIns);
            close(psDel);
        }
    }


    /**
     * This method places the start time of the load into the update_log table
     */
    private void setLastUpdateTime() throws Exception {
        PreparedStatement psUpd = null;
        StringBuffer query = null;

        try {
            int retVal = 0;
            query = new StringBuffer(100);
            query.append("INSERT INTO update_log ");
            query.append("      (log_id ");        // 1
            query.append("       ,calendar_id ");  // 2
            query.append("       ,timestamp  ");   // 3
            query.append("       ,log_type_id) ");   // 4
            query.append("VALUES (0, ?, ?, " + ROUND_LOG_TYPE + ")");
            psUpd = prepareStatement(query.toString(), TARGET_DB);

            int calendar_id = lookupCalendarId(fStartTime, TARGET_DB);
            psUpd.setInt(1, calendar_id);
            psUpd.setTimestamp(2, fStartTime);

            retVal = psUpd.executeUpdate();
            if (retVal != 1) {
                throw new SQLException("SetLastUpdateTime updated " + retVal +
                        " rows, not just one.");
            }
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Failed to set last log time.\n" +
                    sqle.getMessage());
        } finally {
            close(psUpd);
        }
    }


}
