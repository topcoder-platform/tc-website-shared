package com.topcoder.shared.util.dwload;

/**
 * TCLoadPayments.java
 *
 * TCLoadPayments loads coder information tables from one database to another.
 * The tables that are built by this load procedure are:
 * <ul>
 * <li>payment</li>
 * </ul>
 *
 * @author pulky
 * @version 1.0.0
 */

import com.topcoder.shared.util.DBMS;
import com.topcoder.shared.util.logging.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;

public class TCLoadPayments extends TCLoad {
    private static final int PAYMENTS_LOG_TYPE = 6;
    private static Logger log = Logger.getLogger(TCLoadPayments.class);
    protected java.sql.Timestamp fStartTime = null;
    protected java.sql.Timestamp fLastLogTime = null;

    public TCLoadPayments() {
        DEBUG = false;
    }

    /**
     * This method is passed any parameters passed to this load
     */
    public boolean setParameters(Hashtable params) {
        return true;
    }

    /**
     * This method performs the load for the payments tables
     */
    public void performLoad() throws Exception {
        try {
            fStartTime = new java.sql.Timestamp(System.currentTimeMillis());

            getLastUpdateTime();

            loadPayments();

            setLastUpdateTime();

            log.info("SUCCESS: Payments load ran successfully.");
        } catch (Exception ex) {
            setReasonFailed(ex.getMessage());
            throw ex;
        }
    }

    private void getLastUpdateTime() throws Exception {
        Statement stmt = null;
        ResultSet rs = null;
        StringBuffer query = null;

        query = new StringBuffer(100);
        query.append("select timestamp from update_log where log_id = ");
        query.append("(select max(log_id) from update_log where log_type_id = " + PAYMENTS_LOG_TYPE + ")");

        try {
            stmt = createStatement(TARGET_DB);
            rs = stmt.executeQuery(query.toString());
            if (rs.next()) {
                fLastLogTime = rs.getTimestamp(1);
                log.info("Date is " + fLastLogTime.toString());
            } else {
                // A little misleading here as we really didn't hit a SQL
                // exception but all we are doing outside this method is
                // catchin and setting the reason for failure to be the
                // message of the exception.
                throw new SQLException("Last log time not found in " +
                        "update_log table.");
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

    private void loadPayments() throws Exception {
        int count = 0;
        int retVal = 0;
        PreparedStatement psSel = null;
        PreparedStatement psIns = null;
        PreparedStatement psDel = null;
        PreparedStatement psSelModified = null;
        ResultSet modifiedPayments = null;
        ResultSet rs = null;
        StringBuffer query = null;

        try {
            boolean paymentsFound = false;

            // this is to avoid a gigantic delete query the firs time the script is run.
            if (fLastLogTime.before(new Date((new GregorianCalendar(1990,1,1)).getTimeInMillis()))) {
                paymentsFound = true;
            } else {
                query = new StringBuffer(100);
                query.append("select distinct pdx.payment_id from payment_detail pd, payment_detail_xref pdx ");
                query.append("where pd.payment_detail_id = pdx.payment_detail_id ");
                query.append("and pd.date_modified > ? ");
                psSelModified = prepareStatement(query.toString(), SOURCE_DB);
                psSelModified.setTimestamp(1, fLastLogTime);
    
                query = new StringBuffer(100);
                query.append("delete from payment ");
                query.append("where payment_id in ( ");
    
                modifiedPayments = psSelModified.executeQuery();
                while (modifiedPayments.next()) {
                    paymentsFound = true;
                    query.append(modifiedPayments.getLong("payment_id"));
                    query.append(",");
                }
                query.setCharAt(query.length() - 1, ')');

                if (paymentsFound) {
                    // delete modified payments
                    psDel = prepareStatement(query.toString(), TARGET_DB);
                    psDel.executeUpdate();
                }
            }
            
            if (paymentsFound) {
                // insert modified payments
                query = new StringBuffer(100);
                query.append("select payment_id, user_id, net_amount, gross_amount, payment_desc, ");
                query.append("pd.payment_type_id, payment_type_desc, date_due, algorithm_round_id, algorithm_problem_id, ");
                query.append("component_contest_id, component_project_id, studio_contest_id, ");
                query.append("digital_run_stage_id, digital_run_season_id, parent_payment_id "); 
                query.append("from payment_detail pd, payment p, payment_type_lu ptl ");
                query.append("where pd.payment_detail_id = p.most_recent_detail_id ");
                query.append("and ptl.payment_type_id = pd.payment_type_id ");
                query.append("and pd.date_modified > ? ");
                psSel = prepareStatement(query.toString(), SOURCE_DB);
                psSel.setTimestamp(1, fLastLogTime);
                    
                query = new StringBuffer(100);
                query.append("insert into payment (payment_id, user_id, net_amount, gross_amount, payment_desc, payment_type_id, ");
                query.append("payment_type_desc, date_due, algorithm_round_id, algorithm_problem_id, component_contest_id, "); 
                query.append("component_project_id, studio_contest_id, digital_run_stage_id,  ");
                query.append("digital_run_season_id, parent_payment_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
                psIns = prepareStatement(query.toString(), TARGET_DB);

            
                rs = psSel.executeQuery();
    
                while (rs.next()) {
                    psIns.clearParameters();
                    psIns.setLong(1, rs.getLong("payment_id"));
                    psIns.setLong(2, rs.getLong("user_id"));
                    psIns.setDouble(3, rs.getDouble("net_amount"));
                    psIns.setDouble(4, rs.getDouble("gross_amount"));
                    psIns.setString(5, rs.getString("payment_desc"));
                    psIns.setLong(6, rs.getLong("payment_type_id"));
                    psIns.setString(7, rs.getString("payment_type_desc"));
                    psIns.setDate(8, rs.getDate("date_due"));
                    psIns.setLong(9, rs.getLong("algorithm_round_id"));
                    psIns.setLong(10, rs.getLong("algorithm_problem_id"));
                    psIns.setLong(11, rs.getLong("component_contest_id"));
                    psIns.setLong(12, rs.getLong("component_project_id"));
                    psIns.setLong(13, rs.getLong("studio_contest_id"));
                    psIns.setLong(14, rs.getLong("digital_run_stage_id"));
                    psIns.setLong(15, rs.getLong("digital_run_season_id"));
                    psIns.setLong(16, rs.getLong("parent_payment_id"));
                    retVal = psIns.executeUpdate();
    
                    count = count + retVal;
                    if (retVal != 1) {
                        throw new SQLException("TCLoadPayments: Load payment for payment_id " + rs.getLong("payment_id") +
                                " could not be inserted.");
                    }
    
                    printLoadProgress(count, "payments");
                }
            }
            log.info("payment records copied = " + count);
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'payment' table failed.\n" +
                    sqle.getMessage());
        } finally {
            DBMS.close(modifiedPayments);
            DBMS.close(rs);            
            DBMS.close(psSel);
            DBMS.close(psIns);
            DBMS.close(psDel);
            DBMS.close(psSelModified);
        }
    }

    private void setLastUpdateTime() throws Exception {
        PreparedStatement psUpd = null;
        StringBuffer query = null;

        try {
            int retVal = 0; 
            query = new StringBuffer(100);
            query.append("INSERT INTO update_log ");
            query.append("      (log_id ");        // 1
            query.append("       ,calendar_id ");  // 2
            query.append("       ,timestamp ");   // 3
            query.append("       ,log_type_id) ");   // 4
            query.append("VALUES (0, ?, ?, " + PAYMENTS_LOG_TYPE + ")");
            psUpd = prepareStatement(query.toString(), TARGET_DB);

            int calendar_id = lookupCalendarId(fStartTime, TARGET_DB);
            psUpd.setInt(1, calendar_id);
            psUpd.setTimestamp(2, fStartTime);

            retVal = psUpd.executeUpdate();
            if (retVal != 1) {
                throw new SQLException("SetLastUpdateTime " +
                        " modified " + retVal + " rows, not one.");
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
