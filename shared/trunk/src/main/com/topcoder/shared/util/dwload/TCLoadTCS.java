/*
 * Copyright (c) 2006 TopCoder, Inc. All rights reserved.
 */
package com.topcoder.shared.util.dwload;

import com.topcoder.shared.distCache.CacheClient;
import com.topcoder.shared.distCache.CacheClientFactory;
import com.topcoder.shared.util.DBMS;
import com.topcoder.shared.util.logging.Logger;

import java.sql.*;
import java.util.*;

/**
 * <strong>Purpose</strong>:
 * Extends from <strong>TCLoad</strong> to load TCS date to the DW.
 *
 * Version 1.1.0 Change notes:
 * <ol>
 * <li>
 * Added columns to project, project_result.
 * </li>
 * <li>
 * Added table rookie.
 * </li>
 * </ol>
 *
 * @author rfairfax, pulky
 * @version 1.1.0
 */
public class TCLoadTCS extends TCLoad {
    private static Logger log = Logger.getLogger(TCLoadTCS.class);

    private static final int OVERALL_RATING_RANK_TYPE_ID = 1;
    private static final int ACTIVE_RATING_RANK_TYPE_ID = 2;

    protected java.sql.Timestamp fStartTime = null;
    protected java.sql.Timestamp fLastLogTime = null;
    private int TCS_LOG_TYPE = 4;

    private static final String PROJECT_SELECT =
            "select distinct project_id from project_result";
    
    /**
     * First cut date for rookies in JDBC date escape format.
     * @since 1.1.0
     */
    private static final String FIRST_CUT_DATE = "2006-5-11";
        
    /**
     * Id of the first season for rookies.
     * @since 1.1.0
     */
    private static final long FIRST_SEASON_ID = 1;
    
    /**
     * Confirmed status.
     * @since 1.1.0
     */
    private static final int CONFIRMED = 1;
    
    /**
     * Passed review threshold for being elegible for the ROTY season..
     * @since 1.1.0
     */    
    private static final int PASSED_REVIEW_THRESHOLD = 6;
    
    /**
     * Potential status.
     * @since 1.1.0
     */
    private static final int POTENTIAL = 0;
    
    /**
     * ID for completed status.
     * @since 1.1.0
     */
    private static final int STATUS_COMPLETED = 4;
    
    /**
     * Max place reworded for placement points.
     * @since 1.1.0
     */
    private static final int MAX_PLACE_REWORDED = 7;
    
    /**
     * Max number of submissions for placement points matrix.
     * @since 1.1.0
     */
    private static final int MAX_NUM_SUBMISSIONS = 7;

    /**
     * Placement points matrix
     * @since 1.1.0
     */
    private static final int[][] placementPoints = {{500, 325, 270, 250, 245, 240, 235},
                                                    {  0, 175, 150, 145, 135, 130, 130},
                                                    {  0,   0,  80,  75,  75,  75,  75},
                                                    {  0,   0,   0,  30,  30,  30,  30},
                                                    {  0,   0,   0,   0,  15,  15,  15},
                                                    {  0,   0,   0,   0,   0,  10,  10},
                                                    {  0,   0,   0,   0,   0,   0,   5},
                                                    };

    public TCLoadTCS() {
        DEBUG = false;
    }

    /**
     * This method is passed any parameters passed to this load
     */
    public boolean setParameters(Hashtable params) {
        return true;
    }

    /**
     * This method performs the load for the coder information tables
     */
    public void performLoad() throws Exception {
        try {

            PreparedStatement ps = null;
            try {
                ps = prepareStatement("set lock mode to wait 5", SOURCE_DB);
                ps.execute();
            } finally {
                close(ps);
            }

            fStartTime = new java.sql.Timestamp(System.currentTimeMillis());
            getLastUpdateTime();



            doLoadReviewResp();
            doLoadEvent();
            doLoadUserEvent();

            doLoadContests();

            doLoadContestPrize();

            doLoadProjects();

            doLoadProjectResults();
            
            doLoadRookies();

            doLoadScorecardTemplate();

            doLoadSubmissionReview();
            doLoadSubmissionScreening();

            doLoadContestProject();

            doLoadUserRating();

            doLoadUserReliability();

            doLoadRoyalty();

            doLoadEvaluationLU();

            doLoadScorecardQuestion();

            doLoadScorecardResponse();

            doLoadTestcaseResponse();

            doLoadSubjectiveResponse();

            doLoadAppeal();
            doLoadTestcaseAppeal();


            List list = getCurrentRatings();
            doLoadRank(112, ACTIVE_RATING_RANK_TYPE_ID, list);
            doLoadRank(112, OVERALL_RATING_RANK_TYPE_ID, list);
            doLoadRank(113, ACTIVE_RATING_RANK_TYPE_ID, list);
            doLoadRank(113, OVERALL_RATING_RANK_TYPE_ID, list);

            loadSchoolRatingRank(112, ACTIVE_RATING_RANK_TYPE_ID, list);
            loadSchoolRatingRank(112, OVERALL_RATING_RANK_TYPE_ID, list);
            loadSchoolRatingRank(113, ACTIVE_RATING_RANK_TYPE_ID, list);
            loadSchoolRatingRank(113, OVERALL_RATING_RANK_TYPE_ID, list);

            loadCountryRatingRank(112, ACTIVE_RATING_RANK_TYPE_ID, list);
            loadCountryRatingRank(112, OVERALL_RATING_RANK_TYPE_ID, list);
            loadCountryRatingRank(113, ACTIVE_RATING_RANK_TYPE_ID, list);
            loadCountryRatingRank(113, OVERALL_RATING_RANK_TYPE_ID, list);

            //fix problems with submission date

            //todo what the hell is this?  do we need it?
            final String sSQL = "update project_result " +
                    "         set submit_timestamp = (select max(u.submission_date) " +
                    "         from project p, " +
                    "         user_component_score u " +
                    "         where p.project_id = project_result.project_id " +
                    "         and u.component_name = p.component_name " +
                    "         and u.phase_id = p.phase_id " +
                    "         and u.component_id = p.component_id " +
                    "         and u.user_id = project_result.user_id " +
                    "         and u.score = project_result.final_score " +
                    "         group by p.project_id), submit_ind = 1 " +
                    " where exists(  " +
                    "         select max(u.submission_date) " +
                    "         from project p, " +
                    "         user_component_score u " +
                    "         where p.project_id = project_result.project_id " +
                    "         and u.component_name = p.component_name " +
                    "         and u.phase_id = p.phase_id " +
                    "         and u.component_id = p.component_id " +
                    "         and u.user_id = project_result.user_id " +
                    "         and u.score = project_result.final_score " +
                    "         group by p.project_id " +
                    " )";

            try {
                ps = prepareStatement(sSQL, TARGET_DB);
                ps.executeUpdate();
            } finally {
                close(ps);
            }

            doClearCache();

            setLastUpdateTime();

            log.info("SUCCESS: TCS load ran successfully.");
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            setReasonFailed(("Load failed.\n" +
                    sqle.getMessage()));
            throw sqle;
        } catch (Exception ex) {
            setReasonFailed(ex.getMessage());
            throw ex;
        }
    }

    public void doClearCache() throws Exception {
        CacheClient cc = CacheClientFactory.createCacheClient();

        String tempKey;

        String[] keys = new String[]{"tccc05_", "usdc_", "component_history", "tcs_ratings_history",
                "member_profile", "Coder_Dev_Data", "Coder_Des_Data", "Component_",
                "public_home_data", "top_designers", "top_developers", "tco04",
                "coder_all_ratings", "tco05", "coder_dev", "coder_des", "coder_algo",
                "dd_design", "dd_development", "dd_component", "comp_list", "find_projects", "get_review_scorecard",
                "get_screening_scorecard", "project_info", "reviewers_for_project", "scorecard_details", "submissions",
                "comp_contest_details", "dr_leader_board", "dr_rookie_board", "competition_history", "algo_competition_history", 
                "dr_current_period", "dr_stages", "dr_seasons"   
                };

        ArrayList list = cc.getKeys();
        for (int i = 0; i < list.size(); i++) {
            tempKey = (String) list.get(i);
            for (int j = 0; j < keys.length; j++) {
                if (tempKey.indexOf(keys[j]) > -1) {
                    cc.remove(tempKey);
                    break;
                }
            }
        }
    }

    private void getLastUpdateTime() throws Exception {
        Statement stmt = null;
        ResultSet rs = null;
        StringBuffer query;

        query = new StringBuffer(100);
        query.append("select timestamp from update_log where log_id = ");
        query.append("(select max(log_id) from update_log where log_type_id = " + TCS_LOG_TYPE + ")");

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

    private void setLastUpdateTime() throws Exception {
        PreparedStatement psUpd = null;
        StringBuffer query;

        try {
            int retVal;
            query = new StringBuffer(100);
            query.append("INSERT INTO update_log ");
            query.append("      (log_id ");        // 1
            query.append("       ,calendar_id ");  // 2
            query.append("       ,timestamp ");   // 3
            query.append("       ,log_type_id) ");   // 4
            query.append("VALUES (0, ?, ?, ").append(TCS_LOG_TYPE).append(")");
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


    public void doLoadReviewResp() throws Exception {
        log.info("load review resp");
        ResultSet reviewResp;

        PreparedStatement reviewRespSelect = null;
        PreparedStatement reviewRespUpdate = null;
        PreparedStatement reviewRespInsert = null;

        final String REVIEW_RESP_SELECT = "select review_resp_id, review_resp_name from review_resp";

        final String REVIEW_RESP_UPDATE =
                "update review_resp set review_resp_desc=? where review_resp_id=?";

        final String REVIEW_RESP_INSERT =
                "insert into review_resp (review_resp_id, review_resp_desc) values (?, ?)";


        try {
            long start = System.currentTimeMillis();

            reviewRespSelect = prepareStatement(REVIEW_RESP_SELECT, SOURCE_DB);
            reviewRespUpdate = prepareStatement(REVIEW_RESP_UPDATE, TARGET_DB);
            reviewRespInsert = prepareStatement(REVIEW_RESP_INSERT, TARGET_DB);

            int count = 0;

            reviewResp = reviewRespSelect.executeQuery();

            while (reviewResp.next()) {

                reviewRespUpdate.clearParameters();

                reviewRespUpdate.setObject(1, reviewResp.getObject("review_resp_name"));
                reviewRespUpdate.setLong(2, reviewResp.getLong("review_resp_id"));

                int retVal = reviewRespUpdate.executeUpdate();

                if (retVal == 0) {
                    reviewRespInsert.clearParameters();

                    reviewRespInsert.setLong(1, reviewResp.getLong("review_resp_id"));
                    reviewRespInsert.setObject(2, reviewResp.getObject("review_resp_name"));

                    reviewRespInsert.executeUpdate();
                }
                count++;

            }

            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'review_resp' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(reviewRespInsert);
            close(reviewRespUpdate);
            close(reviewRespSelect);
        }
    }



    public void doLoadRoyalty() throws Exception {
        log.info("load royalties");
        PreparedStatement select = null;
        PreparedStatement insert = null;
        PreparedStatement update = null;
        ResultSet rs = null;
        try {
            long start = System.currentTimeMillis();
            final String SELECT = "select user_id, amount, description, royalty_date from royalty " +
                                  "where modify_date > ?";
            final String UPDATE = "update royalty set amount = ?, description = ? where royalty_date = ? " +
                    " and user_id = ? ";
            final String INSERT = "insert into royalty (user_id, amount, description, royalty_date) " +
                    "values (?, ?, ?, ?) ";

            select = prepareStatement(SELECT, SOURCE_DB);
            select.setTimestamp(1, fLastLogTime);

            update = prepareStatement(UPDATE, TARGET_DB);
            insert = prepareStatement(INSERT, TARGET_DB);
            rs = select.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                //log.debug("PROCESSING USER " + rs.getInt("user_id"));

                //update record, if 0 rows affected, insert record
                update.clearParameters();
                update.setObject(1, rs.getObject("amount"));
                update.setObject(2, rs.getObject("description"));
                update.setObject(3, rs.getObject("royalty_date"));
                update.setLong(4, rs.getLong("user_id"));

                int retVal = update.executeUpdate();

                if (retVal == 0) {
                    //need to insert
                    insert.clearParameters();
                    insert.setLong(1, rs.getLong("user_id"));
                    insert.setObject(2, rs.getObject("amount"));
                    insert.setObject(3, rs.getObject("description"));
                    insert.setObject(4, rs.getObject("royalty_date"));
                    insert.executeUpdate();
                }
            }
            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'royalty' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(insert);
            close(update);
            close(select);
        }
    }

    public void doLoadUserReliability() throws Exception {
        log.info("load user reliability");
        PreparedStatement select = null;
        PreparedStatement insert = null;
        PreparedStatement update = null;
        ResultSet rs = null;

        try {
            long start = System.currentTimeMillis();
            final String SELECT = "select user_id, rating, phase_id from user_reliability where modify_date > ? ";
            final String INSERT = "insert into user_reliability (user_id, rating, phase_id) " +
                    "values (?, ?, ?) ";
            final String UPDATE = "update user_reliability set rating = ?" +
                    " where user_id = ? and phase_id = ?";

            select = prepareStatement(SELECT, SOURCE_DB);
            select.setTimestamp(1, fLastLogTime);

            update = prepareStatement(UPDATE, TARGET_DB);
            insert = prepareStatement(INSERT, TARGET_DB);
            rs = select.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                //log.debug("PROCESSING USER " + rs.getInt("user_id"));

                update.clearParameters();
                update.setDouble(1, rs.getDouble("rating"));
                update.setLong(2, rs.getLong("user_id"));
                update.setLong(3, rs.getLong("phase_id"));

                int retVal = update.executeUpdate();

                if (retVal == 0) {
                    //need to insert
                    insert.clearParameters();
                    insert.setLong(1, rs.getLong("user_id"));
                    insert.setDouble(2, rs.getDouble("rating"));
                    insert.setLong(3, rs.getLong("phase_id"));

                    insert.executeUpdate();
                }

            }
            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'user_reliability' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(select);
            close(insert);
            close(update);
        }
    }

    public void doLoadUserRating() throws Exception {
        log.info("load user rating");
        PreparedStatement select = null;
        PreparedStatement insert = null;
        PreparedStatement update = null;
        ResultSet rs = null;
        try {

            long start = System.currentTimeMillis();
            final String SELECT = "select ur.rating " +
                                     "  , ur.vol " +
                                     "  , ur.rating_no_vol " +
                                     "  , ur.num_ratings " +
                                     "  , ur.last_rated_project_id " +
                                     "  , ur.user_id " +
                                     "  , ur.phase_id " +
                                     "  , (select max(pr.new_rating) " +
                                           " from project_result pr, project p " +
                                          " where pr.user_id = ur.user_id " +
                                            " and pr.project_id = p.project_id " +
                                            " and pr.rating_ind = 1 " +
                                            " and p.project_type_id+111 = ur.phase_id) as highest_rating " +
                                      " , (select min(pr.new_rating) " +
                                           " from project_result pr, project p " +
                                          " where pr.user_id = ur.user_id " +
                                            " and pr.project_id = p.project_id " +
                                            " and pr.rating_ind = 1 " +
                                            " and p.project_type_id+111 = ur.phase_id) as lowest_rating " +
                                  " from user_rating ur " +
                                  " where ur.mod_date_time > ?";

            final String UPDATE = "update user_rating set rating = ?,  vol = ?, rating_no_vol = ?, num_ratings = ?, last_rated_project_id = ?, mod_date_time = CURRENT, highest_rating = ?, lowest_rating = ? " +
                    " where user_id = ? and phase_id = ?";
            final String INSERT = "insert into user_rating (user_id, rating, phase_id, vol, rating_no_vol, num_ratings, last_rated_project_id, mod_date_time, create_date_time, highest_rating, lowest_rating) " +
                    "values (?, ?, ?, ?, ?, ?, ?, CURRENT, CURRENT, ?, ?) ";

            select = prepareStatement(SELECT, SOURCE_DB);
            select.setTimestamp(1, fLastLogTime);

            insert = prepareStatement(INSERT, TARGET_DB);
            update = prepareStatement(UPDATE, TARGET_DB);
            rs = select.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                //log.debug("PROCESSING USER " + rs.getInt("user_id"));

                //update record, if 0 rows affected, insert record
                update.clearParameters();
                update.setObject(1, rs.getObject("rating"));
                update.setObject(2, rs.getObject("vol"));
                update.setObject(3, rs.getObject("rating_no_vol"));
                update.setObject(4, rs.getObject("num_ratings"));
                //ps2.setObject(6, rs.getObject("last_component_rated"));
                update.setObject(5, rs.getObject("last_rated_project_id"));
                update.setInt(6, rs.getInt("highest_rating"));
                update.setInt(7, rs.getInt("lowest_rating"));
                update.setLong(8, rs.getLong("user_id"));
                update.setObject(9, rs.getObject("phase_id"));


                int retVal = update.executeUpdate();

                if (retVal == 0) {
                    //need to insert
                    insert.clearParameters();
                    insert.setLong(1, rs.getLong("user_id"));
                    insert.setObject(2, rs.getObject("rating"));
                    insert.setObject(3, rs.getObject("phase_id"));
                    insert.setObject(4, rs.getObject("vol"));
                    insert.setObject(5, rs.getObject("rating_no_vol"));
                    insert.setObject(6, rs.getObject("num_ratings"));
                    insert.setObject(7, rs.getObject("last_rated_project_id"));
                    insert.setInt(8, rs.getInt("highest_rating"));
                    insert.setInt(9, rs.getInt("lowest_rating"));

                    insert.executeUpdate();
                }

            }
            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");

        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'user_rating' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(select);
            close(update);
            close(insert);
        }
    }

    /**
     * <p>
     * Calculates stage based on a date.
     * </p>
     *
     * @param date The date used to calculate the stage.
     *
     * @return the stage ID.
     * @since 1.1.0
     */
    private long calculateStage(java.sql.Date date) throws Exception {
        PreparedStatement select = null;
        ResultSet rs = null;

        try {
            //get data from source DB
            final String SELECT = "select " +
                                  "   stage_id " +
                                  "from " +
                                  "   stage s, calendar c1, calendar c2 " +
                                  "where " +
                                  "   s.start_calendar_id = c1.calendar_id and " +
                                  "   s.end_calendar_id = c2.calendar_id and " +
                                  "   c1.date <= DATE(?) and " +
                                  "   c2.date >= DATE(?)";

            select = prepareStatement(SELECT, TARGET_DB);
            select.setDate(1, date);
            select.setDate(2, date);

            rs = select.executeQuery();
            if (!rs.next()) {
                throw new Exception("Stage calculation failed for date: " + date.toString() + ". (no stage found)");
            }

            //log.debug("Date " + date.toString() + " has been assigned stageId = " + rs.getLong("stage_id"));
            return (rs.getLong("stage_id"));
            
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Stage calculation failed for date: " + date.toString() + ".\n" + sqle.getMessage());
        } finally {
            close(rs);
            close(select);
        }
    }
    
    /**
     * <p>
     * Load projects to the DW.
     * </p>
     */
     public void doLoadProjects() throws Exception {
        log.info("load projects");
        PreparedStatement select = null;
        PreparedStatement update = null;
        PreparedStatement insert = null;
        ResultSet rs = null;

        try {
            //log.debug("PROCESSING PROJECT " + project_id);
            long start = System.currentTimeMillis();

            //get data from source DB
            final String SELECT = "select p.project_id, cc.component_id, cc.component_name, " +
                    " case when exists (select 1 from component_inquiry where project_id = p.project_id) then " +
                    "  (select count(*) from component_inquiry where project_id = p.project_id) " +
                    "    else  (case when exists (select 1 from component_inquiry where component_id = cc.component_id and version = cv.version)  then " +
                    "            (select count(distinct user_id) from component_inquiry ci1 where component_id = cc.component_id and version = cv.version) else null end) end as num_registrations, " +
                    " case when exists  (select '1' from submission where cur_version = 1 and project_id = p.project_id and submission_type = 1 and is_removed = 0) then " +
                    "     (select count(*) from submission where cur_version = 1 and project_id = p.project_id and submission_type = 1 and is_removed = 0) " +
                    " else (select count(*) from project_result pr where project_id = p.project_id and valid_submission_ind = 1) end " +
                    "   as num_submissions,  " +
                    "  (select count(*) from project_result pr where project_id = p.project_id and valid_submission_ind = 1 and not  " +
                    " exists (select * from submission where cur_version = 1 and project_id = p.project_id and submission_type = 1 and is_removed = 1 and submitter_id = pr.user_id))  " +
                    "    as num_valid_submissions, " +
                    "  (select count(*) from project_result pr where project_id = p.project_id and pr.passed_review_ind = 1) as num_submissions_passed_review, " +
                    " (select avg(case when raw_score is null then 0 else raw_score end) from project_result where project_id = p.project_id and raw_score is not null) as avg_raw_score, " +
                    " (select avg(case when final_score is null then 0 else final_score end) from project_result where project_id = p.project_id and final_score is not null) as avg_final_score, " +
                    " case when p.project_type_id = 1 then 112 else 113 end as phase_id, " +
                    " (select description from phase where phase_id = (case when p.project_type_id = 1 then 112 else 113 end)) as phase_desc, " +
                    " cc.root_category_id as category_id, " +
                    " (select category_name from categories where category_id = case when cc.root_category_id in (5801776,5801778) then 5801776 when cc.root_category_id in (5801777,5801779) then 5801777 else cc.root_category_id end) as category_desc, " +
                    " (select start_date from phase_instance where phase_id = 1 and cur_version = 1 and project_id = p.project_id) as posting_date, " +
                    " (select end_date from phase_instance where phase_id = 1 and cur_version = 1 and project_id = p.project_id) as submitby_date, " +
                    " (select max(level_id) from comp_version_dates where comp_vers_id = p.comp_vers_id and phase_id = p.project_type_id + 111) as level_id, " +
                    " p.complete_date, " +
                    " rp.review_phase_id, " +
                    " rp.review_phase_name," +
                    " ps.project_stat_id," +
                    " ps.project_stat_name, " +
                    " cat.viewable, " +
                    " cv.version as version_id, " +
                    " cv.version_text as version_text, " +
                    " p.rating_date, " +
                    " p.winner_id " +
                    " from project p, " +
                    " comp_versions cv, " +
                    " comp_catalog cc," +
                    " categories cat, " +
                    " phase_instance pi, " +
                    " review_phase rp," +
                    " project_status ps " +
                    " where p.cur_version = 1  " +
                    " and cv.comp_vers_id = p.comp_vers_id " +
                    " and cc.component_id = cv.component_id " +
                    " and cc.root_category_id = cat.category_id " +
                    " and pi.cur_version = 1 " +
                    " and pi.phase_instance_id = p.phase_instance_id " +
                    " and rp.review_phase_id = pi.phase_id " +
                    " and ps.project_stat_id = p.project_stat_id " +
                    " and (p.modify_date > ? OR cv.modify_date > ? OR cc.modify_date > ? OR pi.modify_date > ?)";

            final String UPDATE = "update project set component_name = ?,  num_registrations = ?, " +
                    "num_submissions = ?, num_valid_submissions = ?, avg_raw_score = ?, avg_final_score = ?, " +
                    "phase_id = ?, phase_desc = ?, category_id = ?, category_desc = ?, posting_date = ?, submitby_date " +
                    "= ?, complete_date = ?, component_id = ?, review_phase_id = ?, review_phase_name = ?, " +
                    "status_id = ?, status_desc = ?, level_id = ?, viewable_category_ind = ?, version_id = ?, version_text = ?, " +
                    "rating_date = ?, num_submissions_passed_review=?, winner_id=?, stage_id = ? where project_id = ? ";

            final String INSERT = "insert into project (project_id, component_name, num_registrations, num_submissions, " +
                    "num_valid_submissions, avg_raw_score, avg_final_score, phase_id, phase_desc, " +
                    "category_id, category_desc, posting_date, submitby_date, complete_date, component_id, " +
                    "review_phase_id, review_phase_name, status_id, status_desc, level_id, viewable_category_ind, version_id, " +
                    "version_text, rating_date, num_submissions_passed_review, winner_id, stage_id) " +
                    "values (?, ?, ?, ?, ?, " +
                            "?, ?, ?, ?, ?, " +
                            "?, ?, ?, ?, ?, " +
                            "?, ?, ?, ?, ?, " +
                            "?, ?, ?, ?, ?, " +
                            "?, ?) ";

            select = prepareStatement(SELECT, SOURCE_DB);
            select.setTimestamp(1, fLastLogTime);
            select.setTimestamp(2, fLastLogTime);
            select.setTimestamp(3, fLastLogTime);
            select.setTimestamp(4, fLastLogTime);
            update = prepareStatement(UPDATE, TARGET_DB);
            insert = prepareStatement(INSERT, TARGET_DB);

            rs = select.executeQuery();
            int count = 0;
            while (rs.next()) {

                if (rs.getLong("version_id") > 999) {
                    throw new Exception ("component " + rs.getString("component_name") + " has a version > 999");
                }

                //update record, if 0 rows affected, insert record
                update.setString(1, rs.getString("component_name"));
                update.setObject(2, rs.getObject("num_registrations"));
                update.setInt(3, rs.getInt("num_submissions"));
                update.setInt(4, rs.getInt("num_valid_submissions"));
                update.setObject(5, rs.getObject("avg_raw_score"));
                update.setObject(6, rs.getObject("avg_final_score"));
                update.setInt(7, rs.getInt("phase_id"));
                update.setString(8, rs.getString("phase_desc"));
                update.setInt(9, rs.getInt("category_id"));
                update.setString(10, rs.getString("category_desc"));
                update.setDate(11, rs.getDate("posting_date"));
                update.setDate(12, rs.getDate("submitby_date"));
                update.setDate(13, rs.getDate("complete_date"));
                update.setLong(14, rs.getLong("component_id"));
                update.setLong(15, rs.getLong("review_phase_id"));
                update.setString(16, rs.getString("review_phase_name"));
                update.setLong(17, rs.getLong("project_stat_id"));
                update.setString(18, rs.getString("project_stat_name"));
                update.setLong(19, rs.getLong("level_id"));
                update.setInt(20, rs.getInt("viewable"));
                update.setInt(21, (int) rs.getLong("version_id"));
                update.setString(22, rs.getString("version_text"));
                update.setDate(23, rs.getDate("rating_date"));
                update.setInt(24, rs.getInt("num_submissions_passed_review"));
                if (rs.getString("winner_id")==null) {
                    update.setNull(25, Types.DECIMAL);
                } else {
                    update.setLong(25, rs.getLong("winner_id"));
                }
                
                
                
                if (rs.getDate("posting_date") == null) {
                    update.setNull(26, Types.DATE);
                } else {
                    try {
                        update.setLong(26, calculateStage(rs.getDate("posting_date")));
                    } catch (Exception e) {
                        update.setNull(26, Types.DATE);                        
                    }
                }
                update.setLong(27, rs.getLong("project_id"));
                
                int retVal = update.executeUpdate();

                if (retVal == 0) {
                    //need to insert

                    insert.setLong(1, rs.getLong("project_id"));
                    insert.setString(2, rs.getString("component_name"));
                    insert.setObject(3, rs.getObject("num_registrations"));
                    insert.setInt(4, rs.getInt("num_submissions"));
                    insert.setInt(5, rs.getInt("num_valid_submissions"));
                    insert.setDouble(6, rs.getDouble("avg_raw_score"));
                    insert.setDouble(7, rs.getDouble("avg_final_score"));
                    insert.setInt(8, rs.getInt("phase_id"));
                    insert.setString(9, rs.getString("phase_desc"));
                    insert.setInt(10, rs.getInt("category_id"));
                    insert.setString(11, rs.getString("category_desc"));
                    insert.setDate(12, rs.getDate("posting_date"));
                    insert.setDate(13, rs.getDate("submitby_date"));
                    insert.setDate(14, rs.getDate("complete_date"));
                    insert.setLong(15, rs.getLong("component_id"));
                    insert.setLong(16, rs.getLong("review_phase_id"));
                    insert.setString(17, rs.getString("review_phase_name"));
                    insert.setLong(18, rs.getLong("project_stat_id"));
                    insert.setString(19, rs.getString("project_stat_name"));
                    insert.setLong(20, rs.getLong("level_id"));
                    insert.setInt(21, rs.getInt("viewable"));
                    insert.setInt(22, (int) rs.getLong("version_id"));
                    insert.setString(23, rs.getString("version_text"));
                    insert.setDate(24, rs.getDate("rating_date"));
                    insert.setInt(25, rs.getInt("num_submissions_passed_review"));
                    if (rs.getString("winner_id") == null) {
                        insert.setNull(26, Types.DECIMAL);
                    } else {
                        insert.setLong(26, rs.getLong("winner_id"));
                    }
                    if (rs.getDate("posting_date") == null) {
                        insert.setNull(27, Types.DATE);
                    } else {
                        try {
                            insert.setLong(27, calculateStage(rs.getDate("posting_date")));
                        } catch (Exception e) {
                            insert.setNull(27, Types.DATE);
                        }
                    }
                    insert.executeUpdate();
                }
                count++;

            }
            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'project_result / project' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(select);
            close(insert);
            close(update);
        }
    }
    
    /**
     * <p>
     * Calculates points awarded based on the defined placementPoints matrix.
     * </p>
     *
     * @param passedReview true if submission passed review
     * @param placed The submission placement
     *
     * @return the points awarded.
     * @since 1.1.0
     */
    private long calculatePointsAwarded(boolean passedReview, int placed, int numSubmissionsPassedReview) {
        // If not passed review or placed too far, there are no chances of winning points
        if (numSubmissionsPassedReview == 0 || !passedReview || placed > MAX_PLACE_REWORDED || placed == 0 ) {
            return 0;
        }

        // If there are more submissions, stick to the last points
        if (numSubmissionsPassedReview > MAX_NUM_SUBMISSIONS) {
            numSubmissionsPassedReview = MAX_NUM_SUBMISSIONS;
        }

        //log.debug("passedReviewCount: " + numSubmissionsPassedReview);
        //log.debug("placed: " + placed);
        return (placementPoints[placed - 1][numSubmissionsPassedReview - 1]);
    }

    /**
     * <p>
     * Gets all projects with a defined stage.
     * </p>
     *
     * @return a list containing the DR project IDs.
     * @since 1.1.0
     */
    private List getDRProjects() throws Exception{
        PreparedStatement select = null;
        ResultSet rs = null;

        ArrayList dRProjects = new ArrayList();
        try {
            //get data from source DB
            final String SELECT = "select " +
                                  "   project_id, stage_id " +
                                  "from " +
                                  "   project " +
                                  "where " +
                                  "   stage_id is not null";

            select = prepareStatement(SELECT, TARGET_DB);

            rs = select.executeQuery();
            while (rs.next()) {
                dRProjects.add(new Long(rs.getLong("project_id")));
            }

        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("could not get DR projects.");
        } finally {
            close(rs);
            close(select);
        }
        return (dRProjects);
    }
      
          
    /**
     * <p>
     * Load projects results to the DW.
     * </p>
     */
    public void doLoadProjectResults() throws Exception {
        log.info("load project results");
        ResultSet projectResults = null;
        PreparedStatement resultUpdate = null;
        PreparedStatement resultInsert = null;
        PreparedStatement resultSelect = null;


        final String RESULT_SELECT =
                "select pr.project_id, pr.user_id, " +
                "case when exists(select '1' from submission s where s.cur_version = 1 and s.project_id = pr.project_id " +
                "and s.submitter_id = pr.user_id and submission_type = 1 and is_removed = 0) then 1 " +
                "when exists(select '1' from submission s where s.cur_version = 1 and s.project_id = pr.project_id " +
                "and s.submitter_id = pr.user_id and submission_type = 1 and is_removed = 1) then 0 "+
                "else pr.valid_submission_ind end as submit_ind, " +
                "case  when exists(select '1' from submission s where s.cur_version = 1 and s.project_id = pr.project_id " +
                "and s.submitter_id = pr.user_id and submission_type = 1 and is_removed = 1) then 0 " +
                "else pr.valid_submission_ind  end as valid_submission_ind, " +
                "pr.raw_score, " +
                "pr.final_score, " +
                "case when exists (select create_time from component_inquiry where project_id = p.project_id and user_id = pr.user_id) then " +
                "(select min(create_time) from component_inquiry where project_id = p.project_id and user_id = pr.user_id) else " +
                "(select min(create_time) from component_inquiry where component_id = cc.component_id and user_id = pr.user_id) end as inquire_timestamp, " +
                "(select submission_date from submission s where s.cur_version = 1 and s.project_id = pr.project_id and s.submitter_id = pr.user_id and submission_type = 1 and is_removed = 0) as submit_timestamp, " +
                "(select max(pm_review_timestamp) from scorecard where scorecard_type = 2 and is_completed = 1 and submission_id = " +
                "   (select submission_id from submission s where s.cur_version = 1 and s.project_id = pr.project_id and s.submitter_id = pr.user_id and submission_type = 1 and is_removed = 0) " +
                " and project_id = pr.project_id and cur_version = 1) as review_completed_timestamp, " +
                "(select count(*) from project_result pr where project_id = p.project_id and pr.passed_review_ind = 1) as num_submissions_passed_review, " +
                "pr.payment, pr.old_rating, pr.new_rating, " +
                "pr.old_reliability, pr.new_reliability, pr.placed, pr.rating_ind, pr.reliability_ind, pr.passed_review_ind, p.project_stat_id, pr.point_adjustment " +
                "from project_result pr, " +
                "project p, " +
                "comp_versions cv, " +
                "comp_catalog cc " +
                "where p.project_id = pr.project_id " +
                "and p.cur_version = 1  " +
                "and cv.comp_vers_id = p.comp_vers_id " +
                "and cc.component_id = cv.component_id " +
                "and (p.modify_date > ? OR cv.modify_date > ? OR cc.modify_date > ? OR pr.modify_date > ?)";

        final String RESULT_UPDATE =
                "update project_result set submit_ind = ?, valid_submission_ind = ?, raw_score = ?, final_score = ?, inquire_timestamp = ?, " +
                "submit_timestamp = ?, review_complete_timestamp = ?, payment = ?, old_rating = ?, new_rating = ?, old_reliability = ?, new_reliability = ?, " +
                "placed = ?, rating_ind = ?, reliability_ind = ?, passed_review_ind = ?, points_awarded = ?, final_points = ?  where project_id = ? and user_id = ?";

        final String RESULT_INSERT =
                "insert into project_result (project_id, user_id, submit_ind, valid_submission_ind, raw_score, final_score, inquire_timestamp," +
                " submit_timestamp, review_complete_timestamp, payment, old_rating, new_rating, old_reliability, new_reliability, placed, rating_ind, " +
                "reliability_ind, passed_review_ind, points_awarded, final_points) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            long start = System.currentTimeMillis();

            List dRProjects = getDRProjects();
            
            resultSelect = prepareStatement(RESULT_SELECT, SOURCE_DB);
            resultSelect.setTimestamp(1, fLastLogTime);
            resultSelect.setTimestamp(2, fLastLogTime);
            resultSelect.setTimestamp(3, fLastLogTime);
            resultSelect.setTimestamp(4, fLastLogTime);
            resultUpdate = prepareStatement(RESULT_UPDATE, TARGET_DB);
            resultInsert = prepareStatement(RESULT_INSERT, TARGET_DB);

            int count = 0;
            //log.debug("PROCESSING PROJECT RESULTS " + project_id);

//            log.debug("before result select");
            projectResults = resultSelect.executeQuery();
            //log.debug("after result select");

            while (projectResults.next()) {
                long project_id = projectResults.getLong("project_id");
                boolean passedReview = false;
                try {
                    passedReview = projectResults.getInt("passed_review_ind") == 1 ? true : false;
                } catch (Exception e) {
                    // do nothing
                }
                
                int placed = 0;
                try {
                    placed = projectResults.getInt("placed");
                } catch (Exception e) {
                    // do nothing
                }

                int numSubmissionsPassedReview = 0;
                try {
                    numSubmissionsPassedReview = projectResults.getInt("num_submissions_passed_review");
                } catch (Exception e) {
                    // do nothing
                }
                    
                count++;
                resultUpdate.clearParameters();


                resultUpdate.setObject(1, projectResults.getObject("submit_ind"));
                resultUpdate.setObject(2, projectResults.getObject("valid_submission_ind"));
                resultUpdate.setObject(3, projectResults.getObject("raw_score"));
                resultUpdate.setObject(4, projectResults.getObject("final_score"));
                resultUpdate.setObject(5, projectResults.getObject("inquire_timestamp"));
                resultUpdate.setObject(6, projectResults.getObject("submit_timestamp"));
                resultUpdate.setObject(7, projectResults.getObject("review_completed_timestamp"));
                resultUpdate.setObject(8, projectResults.getObject("payment"));
                resultUpdate.setObject(9, projectResults.getObject("old_rating"));
                resultUpdate.setObject(10, projectResults.getObject("new_rating"));
                resultUpdate.setObject(11, projectResults.getObject("old_reliability"));
                resultUpdate.setObject(12, projectResults.getObject("new_reliability"));
                resultUpdate.setObject(13, projectResults.getObject("placed"));
                resultUpdate.setObject(14, projectResults.getObject("rating_ind"));
                resultUpdate.setObject(15, projectResults.getObject("reliability_ind"));
                resultUpdate.setObject(16, projectResults.getObject("passed_review_ind"));
                
                long pointsAwarded = 0;
                if (projectResults.getLong("project_stat_id") == STATUS_COMPLETED &&
                    dRProjects.contains(new Long(project_id))) {
                    pointsAwarded = calculatePointsAwarded(passedReview, placed, numSubmissionsPassedReview);
                    resultUpdate.setLong(17, pointsAwarded);
                    // adjusts final points. point_adjustment could be negative to substracto points.
                    resultUpdate.setLong(18, pointsAwarded + projectResults.getInt("point_adjustment"));
                } else {
                    resultUpdate.setNull(17, Types.DECIMAL);
                    resultUpdate.setNull(18, Types.DECIMAL);
                }
                resultUpdate.setLong(19, project_id);
                resultUpdate.setLong(20, projectResults.getLong("user_id"));

                //log.debug("before result update");
                int retVal = resultUpdate.executeUpdate();
                //log.debug("after result update");

                if (retVal == 0) {

                    resultInsert.clearParameters();

                    resultInsert.setLong(1, project_id);
                    resultInsert.setLong(2, projectResults.getLong("user_id"));
                    resultInsert.setObject(3, projectResults.getObject("submit_ind"));
                    resultInsert.setObject(4, projectResults.getObject("valid_submission_ind"));
                    resultInsert.setObject(5, projectResults.getObject("raw_score"));
                    resultInsert.setObject(6, projectResults.getObject("final_score"));
                    resultInsert.setObject(7, projectResults.getObject("inquire_timestamp"));
                    resultInsert.setObject(8, projectResults.getObject("submit_timestamp"));
                    resultInsert.setObject(9, projectResults.getObject("review_completed_timestamp"));
                    resultInsert.setObject(10, projectResults.getObject("payment"));
                    resultInsert.setObject(11, projectResults.getObject("old_rating"));
                    resultInsert.setObject(12, projectResults.getObject("new_rating"));
                    resultInsert.setObject(13, projectResults.getObject("old_reliability"));
                    resultInsert.setObject(14, projectResults.getObject("new_reliability"));
                    resultInsert.setObject(15, projectResults.getObject("placed"));
                    resultInsert.setObject(16, projectResults.getObject("rating_ind"));
                    resultInsert.setObject(17, projectResults.getObject("reliability_ind"));
                    resultInsert.setObject(18, projectResults.getObject("passed_review_ind"));

                    if (projectResults.getLong("project_stat_id") == STATUS_COMPLETED &&
                        dRProjects.contains(new Long(project_id))) {
                        resultInsert.setLong(19, pointsAwarded);
                        resultInsert.setLong(20, pointsAwarded + projectResults.getInt("point_adjustment"));
                    } else {
                        resultInsert.setNull(19, Types.DECIMAL);
                        resultInsert.setNull(20, Types.DECIMAL);
                    }
                    //log.debug("before result insert");
                    resultInsert.executeUpdate();
                    //log.debug("after result insert");

                }
                //printLoadProgress(count, "project result");
            }
            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'project_result / project' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(projectResults);
            close(resultUpdate);
            close(resultInsert);
            close(resultSelect);
        }
    }

    /**
     * <p>
     * Retrieves the seasons ID's from DB sorted by date.
     * </p>
     *
     * @return a sorted list by date of the seasons ID's.
     * @since 1.1.0
     */
     private List getSeasons() throws Exception {
        PreparedStatement selectSeasons = null;
        ResultSet rsSeasons = null;
        final String SELECT_SEASONS = "select season_id, start_calendar_id from season order by start_calendar_id asc";
        selectSeasons = prepareStatement(SELECT_SEASONS, TARGET_DB);
        rsSeasons = selectSeasons.executeQuery();
        List arrayList = new ArrayList();
        while (rsSeasons.next()) {
            arrayList.add(new Long(rsSeasons.getLong("season_id")));
            // log.info("New season: " + rsSeasons.getLong("season_id"));
        }
        return (arrayList);
    }
    
    /**
     * <p>
     * Retrieves next season id based on a particular season.
     * </p>
     *
     * @param seasons The seasons list
     * @param season The season to retrieve the next one
     *
     * @return the next season after season.
     * @since 1.1.0
     */
     private long getNextSeason(List seasons, long season) throws Exception {
        int i = seasons.indexOf(new Long(season));
        return (((Long)seasons.get(i + 1)).longValue());
    }    
    
    /**
     * <p>
     * Load rookies to the DW.
     * @since 1.1.0
     * </p>
     */
     public void doLoadRookies() throws Exception {
        log.info("regenerating rookies");
        PreparedStatement selectEdge = null;
        PreparedStatement selectUsers = null;
        PreparedStatement selectSubmissions = null;
        PreparedStatement delete = null;
        PreparedStatement insert = null;
        ResultSet rsEdge = null;
        ResultSet rsUsers = null;
        ResultSet rsSubmissions = null;

        List seasons = getSeasons();
        
        try {
            long start = System.currentTimeMillis();
            
            final String SELECT_EDGE = "select pr.user_id, p.phase_id, count(*) num_passed_review from project_result pr, project p " +
                "where pr.project_id = p.project_id and pr.passed_review_ind = 1 and DATE(p.posting_date) <= ? " +
                "and not exists (select 'rookie_already_inserted' from rookie where " +
                "user_id = pr.user_id and phase_id = p.phase_id and season_id = ?) " +
                "group by pr.user_id, p.phase_id having count(*) < " + PASSED_REVIEW_THRESHOLD;
            
            // this query will retrieve users and their first season (when can be calculated), discriminating between
            // development and design.
            final String SELECT_USERS = "select distinct project_result.user_id, project.phase_id, " +
                "( " +
                    "select distinct s.season_id from project p, season s, stage st " +
                    "where s.season_id = st.season_id and p.stage_id = st.stage_id and  " +
                    "p.posting_date = ( " +
                    "	select min(p.posting_date) from project p, project_result pr where   " +
                    "     	p.project_id = pr.project_id and p.phase_id = project.phase_id and   " +
                    "    	pr.passed_review_ind = 1 and user_id = project_result.user_id " +
                    ") " +
                ") as first_season " +
                "from project, project_result " +
                "where project.project_id = project_result.project_id and " +
                "exists 	(	 " +
                "	select s.season_id from project p, season s, stage st " +
                "	where s.season_id = st.season_id and p.stage_id = st.stage_id and  " +
                "	p.posting_date = ( " +
                "		select min(p.posting_date) from project p, project_result pr where   " +
                "		     	p.project_id = pr.project_id and p.phase_id = project.phase_id and   " +
                "		    	pr.passed_review_ind = 1 and user_id = project_result.user_id " +
                "	) " +
                ")";
        
            // this query will retrieve the number of passing submissions for a particular user and phase 
	        // for his first and second season (previously calculated)
            final String SELECT_SUBMISSIONS = "select st.season_id, count(*) as num_submissions from project_result pr, " +
                "project p, stage st " +
                "where passed_review_ind = 1 and pr.project_id = p.project_id and p.stage_id = st.stage_id and " +
                "user_id = ? and p.phase_id = ? and " +
                "st.season_id in (?, ?) " +
                "group by st.season_id " +
                "order by st.season_id asc";

            final String DELETE = "delete from rookie";

            final String INSERT = "insert into rookie (user_id, season_id, phase_id, confirmed_ind) " +
                    "values (?, ?, ?, ?) ";

            selectEdge = prepareStatement(SELECT_EDGE, TARGET_DB);
            selectEdge.setDate(1, java.sql.Date.valueOf(FIRST_CUT_DATE));
            selectEdge.setLong(2, FIRST_SEASON_ID);            
            selectUsers = prepareStatement(SELECT_USERS, TARGET_DB);
            delete = prepareStatement(DELETE, TARGET_DB);
            insert = prepareStatement(INSERT, TARGET_DB);
            selectSubmissions = prepareStatement(SELECT_SUBMISSIONS, TARGET_DB);
            
            // the process will delete all rookies and reload them again completly.
            delete.executeUpdate();
            
            // Stationary state: 
            // - if the user had in his first season more than PASSED_REVIEW_THRESHOLD submissions, 
            //   he is confirmed for that season.
            // - Otherwise he is potential for that season and confirmed for the next one.
            rsUsers = selectUsers.executeQuery();
            int count = 0;
            while (rsUsers.next()) {
                long subFirstSeason = 0;
                long subSecondSeason = 0;
                long firstSeason = rsUsers.getLong("first_season");
                long secondSeason = getNextSeason(seasons, firstSeason);
                long userId = rsUsers.getLong("user_id");
                long phaseId = rsUsers.getLong("phase_id");

                // log.info("New rookie: " + userId + "(" + phaseId + ") - " + firstSeason);
                // log.info("Next season: "  +  secondSeason);
                
                selectSubmissions.setLong(1, userId);
                selectSubmissions.setLong(2, phaseId);
                selectSubmissions.setLong(3, firstSeason);
                selectSubmissions.setLong(4, secondSeason);
                rsSubmissions = selectSubmissions.executeQuery();
                
                // this should be always, since if it's the first season, it must have submissions.
                if (rsSubmissions.next()) {
                    subFirstSeason = rsSubmissions.getLong("num_submissions");
                }
                
                // if there's no next record, second season has no submissions.
                if (rsSubmissions.next()) {
                    subSecondSeason = rsSubmissions.getLong("num_submissions");
                }
                
                // if in his first season, he had more than PASSED_REVIEW_THRESHOLD submissions, he is confirmed for that season.
                if (subFirstSeason >= PASSED_REVIEW_THRESHOLD) {
                    insert.setLong(1, userId);
                    insert.setLong(2, firstSeason);
                    insert.setLong(3, phaseId);
                    insert.setInt(4, CONFIRMED);
                    insert.executeUpdate();
                    count++;
                    // log.info("(1) First submissions: "  + subFirstSeason + " - Second submissions: " + subSecondSeason);
                } else {
                    // else, he is potential for firstSeason and confirmed for secondSeason
                    insert.setLong(1, userId);
                    insert.setLong(2, firstSeason);
                    insert.setLong(3, phaseId);
                    insert.setInt(4, POTENTIAL);
                    insert.executeUpdate();
                    insert.setLong(2, secondSeason);
                    insert.setInt(4, CONFIRMED);
                    insert.executeUpdate();
                    count+=2;
                    // log.info("(2) First submissions: "  + subFirstSeason + " - Second submissions: " + subSecondSeason);
                }
            }

            // Edge case: First rookies will be those having less than PASSED_REVIEW_THRESHOLD submissions prior to the
            // FIRST_CUT_DATE.
            rsEdge = selectEdge.executeQuery();
            while (rsEdge.next()) {
                insert.setLong(1, rsEdge.getLong("user_id"));
                // fixed to the first season
                insert.setLong(2, FIRST_SEASON_ID);
                insert.setLong(3, rsEdge.getLong("phase_id"));
                insert.setInt(4, CONFIRMED);

                insert.executeUpdate();
                count++;
            }            
            
            log.info("" + count + " records generated in " + (System.currentTimeMillis() - start) / 1000 + " seconds");
            
        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Generation of rookie table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rsEdge);
            close(rsUsers);
            close(rsSubmissions);
            close(selectEdge);
            close(selectUsers);
            close(selectSubmissions);
            close(insert);
            close(delete);
        }
    }

    public void doLoadSubmissionReview() throws Exception {
        log.info("load submission review");
        ResultSet submissionInfo = null;
        ResultSet projects = null;
        PreparedStatement submissionSelect = null;
        PreparedStatement submissionUpdate = null;
        PreparedStatement submissionInsert = null;
        PreparedStatement projectSelect = null;

        final String SUBMISSION_SELECT =
                "select sc.project_id,  " +
                "s.submitter_id as user_id,  " +
                "author_id as reviewer_id,  " +
                "sc.raw_score as raw_score, " +
                "score as final_score,  " +
                "(select count(distinct appeal_id) from appeal where appealer_id = s.submitter_id and cur_version = 1  " +
                "and question_id in (select question_id from scorecard_question where scorecard_id = sc.scorecard_id)) as num_appeals,  " +
                //todo populate this
                "0 as num_successful_appeals,  " +
                "rur.r_resp_id as review_resp_id,  " +
                "scorecard_id,  " +
                "(select distinct template_id from question_template qt, scorecard_question sq  " +
                "where qt.q_template_v_id = sq.q_template_v_id and sq.cur_version = 1 and qt.cur_version = 1 and sq.scorecard_id = sc.scorecard_id)  as scorecard_template_id  " +
                "from scorecard sc, submission s, r_user_role rur " +
                "where s.cur_version = 1  " +
                "and s.submission_id = sc.submission_id  " +
                "and rur.project_id = sc.project_id " +
                "and rur.cur_version = 1 " +
                "and rur.login_id = sc.author_id " +
                "and rur.r_role_id = 3 " +
                "and s.submission_type = 1   " +
                "and s.is_removed = 0  " +
                "and sc.project_id = ? " +
                "and sc.scorecard_type = 2 " +
                "and sc.is_completed = 1 " +
                "and sc.cur_version = 1 " +
                "and (sc.modify_date > ? OR s.modify_date > ? OR rur.modify_date > ?)";


        final String SUBMISSION_UPDATE =
                "update submission_review set raw_score = ?, final_score = ?, num_appeals = ?, num_successful_appeals = ?, review_resp_id = ?,  scorecard_id = ?, scorecard_template_id = ? " +
                "where project_id = ? and user_id = ? and reviewer_id = ?";

        final String SUBMISSION_INSERT =
                "insert into submission_review (project_id, user_id, reviewer_id, raw_score, final_score, num_appeals, " +
                "num_successful_appeals, review_resp_id, scorecard_id, scorecard_template_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


        try {
            long start = System.currentTimeMillis();

            submissionSelect = prepareStatement(SUBMISSION_SELECT, SOURCE_DB);
            submissionUpdate = prepareStatement(SUBMISSION_UPDATE, TARGET_DB);
            submissionInsert = prepareStatement(SUBMISSION_INSERT, TARGET_DB);
            projectSelect = prepareStatement(PROJECT_SELECT, SOURCE_DB);

            int count = 0;
            //log.debug("PROCESSING PROJECT RESULTS " + project_id);


            projects = projectSelect.executeQuery();

            while (projects.next()) {
                submissionSelect.clearParameters();
                submissionSelect.setLong(1, projects.getLong("project_id"));
                submissionSelect.setTimestamp(2, fLastLogTime);
                submissionSelect.setTimestamp(3, fLastLogTime);
                submissionSelect.setTimestamp(4, fLastLogTime);
                //log.debug("before submission select");
                submissionInfo = submissionSelect.executeQuery();
                //log.debug("after submission select");

                while (submissionInfo.next()) {

                    submissionUpdate.clearParameters();

                    submissionUpdate.setObject(1, submissionInfo.getObject("raw_score"));
                    submissionUpdate.setObject(2, submissionInfo.getObject("final_score"));
                    submissionUpdate.setObject(3, submissionInfo.getObject("num_appeals"));
                    submissionUpdate.setObject(4, submissionInfo.getObject("num_successful_appeals"));
                    submissionUpdate.setObject(5, submissionInfo.getObject("review_resp_id"));
                    submissionUpdate.setObject(6, submissionInfo.getObject("scorecard_id"));
                    submissionUpdate.setObject(7, submissionInfo.getObject("scorecard_template_id"));
                    submissionUpdate.setLong(8, submissionInfo.getLong("project_id"));
                    submissionUpdate.setLong(9, submissionInfo.getLong("user_id"));
                    submissionUpdate.setLong(10, submissionInfo.getLong("reviewer_id"));


                    //log.debug("before submission update");
                    int retVal = submissionUpdate.executeUpdate();
                    //log.debug("after submission update");

                    if (retVal == 0) {
                        submissionInsert.clearParameters();

                        submissionInsert.setLong(1, submissionInfo.getLong("project_id"));
                        submissionInsert.setLong(2, submissionInfo.getLong("user_id"));
                        submissionInsert.setLong(3, submissionInfo.getLong("reviewer_id"));
                        submissionInsert.setObject(4, submissionInfo.getObject("raw_score"));
                        submissionInsert.setObject(5, submissionInfo.getObject("final_score"));
                        submissionInsert.setObject(6, submissionInfo.getObject("num_appeals"));
                        submissionInsert.setObject(7, submissionInfo.getObject("num_successful_appeals"));
                        submissionInsert.setObject(8, submissionInfo.getObject("review_resp_id"));
                        submissionInsert.setObject(9, submissionInfo.getObject("scorecard_id"));
                        submissionInsert.setObject(10, submissionInfo.getObject("scorecard_template_id"));

                        //log.debug("before submission insert");
                        submissionInsert.executeUpdate();
                        //log.debug("after submission insert");
                    }
                    count++;
                    //printLoadProgress(count, "submission review");
                }
            }


            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'submission review' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(projects);
            close(submissionInfo);
            close(submissionSelect);
            close(submissionUpdate);
            close(submissionInsert);
            close(projectSelect);
        }
    }

    public void doLoadSubmissionScreening() throws Exception {
        log.info("load submission screening");

        ResultSet screenings = null;
        PreparedStatement screeningUpdate = null;
        PreparedStatement screeningInsert = null;
        PreparedStatement screeningSelect = null;


        final String SCREENING_SELECT =
                        "select pr.project_id,  " +
                        "pr.user_id, " +
                        "sc.author_id as reviewer_id,  " +
                        "sc.score as final_score,  " +
                        "sc.scorecard_id,  " +
                        "         (select distinct template_id from question_template qt, scorecard_question sq  " +
                        "         where qt.q_template_v_id = sq.q_template_v_id and sq.cur_version = 1 and qt.cur_version = 1 and sq.scorecard_id = sc.scorecard_id)  as scorecard_template_id  " +
                        "from project_result pr, submission s, scorecard sc " +
                        "where s.cur_version = 1  " +
                        "and s.project_id = pr.project_id  " +
                        "and s.submitter_id = pr.user_id  " +
                        "and s.submission_type = 1  " +
                        "and s.is_removed = 0 " +
                        "and sc.scorecard_type = 1  " +
                        "and sc.is_completed = 1  " +
                        "and sc.submission_id = s.submission_id " +
                        "and sc.project_id = pr.project_id " +
                        "and sc.cur_version = 1 " +
                        "and (pr.modify_date > ? OR sc.modify_date > ?)";


        final String SCREENING_UPDATE =
                "update submission_screening set reviewer_id = ?, final_score = ?, scorecard_id = ?, scorecard_template_id = ? " +
                "where project_id = ? and user_id = ?";

        final String SCREENING_INSERT =
                "insert into submission_screening (project_id, user_id, reviewer_id, final_score, scorecard_id, scorecard_template_id) " +
                "values (?, ?, ?, ?, ?, ?)";

        try {
            long start = System.currentTimeMillis();

            screeningSelect = prepareStatement(SCREENING_SELECT, SOURCE_DB);
            screeningSelect.setTimestamp(1, fLastLogTime);
            screeningSelect.setTimestamp(2, fLastLogTime);
            screeningUpdate = prepareStatement(SCREENING_UPDATE, TARGET_DB);
            screeningInsert = prepareStatement(SCREENING_INSERT, TARGET_DB);

            int count = 0;

            screenings = screeningSelect.executeQuery();

            while (screenings.next()) {
                long project_id = screenings.getLong("project_id");
                count++;
                screeningUpdate.clearParameters();


                screeningUpdate.setObject(1, screenings.getObject("reviewer_id"));
                screeningUpdate.setObject(2, screenings.getObject("final_score"));
                screeningUpdate.setObject(3, screenings.getObject("scorecard_id"));
                screeningUpdate.setObject(4, screenings.getObject("scorecard_template_id"));
                screeningUpdate.setLong(5, project_id);
                screeningUpdate.setLong(6, screenings.getLong("user_id"));

                int retVal = screeningUpdate.executeUpdate();

                if (retVal == 0) {

                    screeningInsert.clearParameters();

                    screeningInsert.setLong(1, project_id);
                    screeningInsert.setLong(2, screenings.getLong("user_id"));
                    screeningInsert.setObject(3, screenings.getObject("reviewer_id"));
                    screeningInsert.setObject(4, screenings.getObject("final_score"));
                    screeningInsert.setObject(5, screenings.getObject("scorecard_id"));
                    screeningInsert.setObject(6, screenings.getObject("scorecard_template_id"));

                    screeningInsert.executeUpdate();


                }
            }
            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of submission_screening table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(screenings);
            close(screeningUpdate);
            close(screeningInsert);
            close(screeningSelect);
        }
    }


    public void doLoadContestProject() throws Exception {
        log.info("load contest project");
        //load contest_project_xref
        long start = System.currentTimeMillis();

        final String SELECT = "select x.contest_id, x.project_id  " +
                "from contest_project_xref x, project p " +
                "where x.project_id = ? and p.project_id = x.project_id " +
                " and p.cur_version = 1 and (p.modify_date > ? or x.create_date > ?)";

        final String INSERT = "insert into contest_project_xref (contest_id, project_id) " +
                "values (?, ?)";
        final String DELETE = "delete from contest_project_xref where contest_id = ? and project_id = ?";


        PreparedStatement delete = null;
        PreparedStatement select = null;
        PreparedStatement insert = null;
        PreparedStatement projectSelect = null;
        ResultSet rs = null;
        ResultSet projects = null;

        long projectId = 0;
        try {
            select = prepareStatement(SELECT, SOURCE_DB);
            projectSelect = prepareStatement(PROJECT_SELECT, SOURCE_DB);

            insert = prepareStatement(INSERT, TARGET_DB);
            delete = prepareStatement(DELETE, TARGET_DB);

            projects = projectSelect.executeQuery();
            int count = 0;
            while (projects.next()) {
                projectId = projects.getLong("project_id");
                select.clearParameters();
                select.setLong(1, projectId);
                select.setTimestamp(2, fLastLogTime);
                select.setTimestamp(3, fLastLogTime);

                rs = select.executeQuery();
                while (rs.next()) {
                    insert.clearParameters();
                    insert.setLong(1, rs.getLong("contest_id"));
                    insert.setLong(2, projectId);
                    try {
                        insert.executeUpdate();
                    } catch (SQLException e) {
                        delete.setLong(1, rs.getLong("contest_id"));
                        delete.setLong(2, projectId);
                        delete.executeUpdate();
                        insert.executeUpdate();
                    }
                    count++;

                }

            }
            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");

        } catch (SQLException e) {
            DBMS.printSqlException(true, e);
            throw new Exception("Load of 'contest_project_xref' failed on project " + projectId);
        } finally {
            close(rs);
            close(projects);
            close(delete);
            close(select);
            close(insert);
            close(projectSelect);
        }
    }


    public void doLoadContests() throws Exception {
        log.info("load contests");
        PreparedStatement select = null;
        PreparedStatement insert = null;
        PreparedStatement update = null;
        ResultSet rs = null;

        try {
            long start = System.currentTimeMillis();
            final String SELECT = "select c.contest_id, c.contest_name, " +
                    "c.start_date as contest_start_timestamp, " +
                    "c.end_date as contest_end_timestamp, " +
                    "c.contest_type_id, " +
                    "ct.contest_type_desc," +
                    "c.phase_id," +
                    "c.event_id  " +
                    "from contest c, " +
                    "contest_type_lu ct " +
                    "where ct.contest_type_id = c.contest_type_id " +
                    "and (c.modify_date > ?)";
            final String UPDATE = "update contest set contest_name = ?,  contest_start_timestamp = ?, contest_end_timestamp = ?, contest_type_id = ?, contest_type_desc = ?, phase_id = ?, event_id = ? " +
                    " where contest_id = ? ";
            final String INSERT = "insert into contest (contest_id, contest_name, contest_start_timestamp, contest_end_timestamp, contest_type_id, contest_type_desc, phase_id, event_id) " +
                    "values (?, ?, ?, ?, ?, ?, ?, ?) ";

            select = prepareStatement(SELECT, SOURCE_DB);
            select.setTimestamp(1, fLastLogTime);
            update = prepareStatement(UPDATE, TARGET_DB);
            insert = prepareStatement(INSERT, TARGET_DB);
            rs = select.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                //log.debug("PROCESSING CONTEST " + rs.getInt("contest_id"));

                //update record, if 0 rows affected, insert record
                update.clearParameters();
                update.setObject(1, rs.getObject("contest_name"));
                update.setObject(2, rs.getObject("contest_start_timestamp"));
                update.setObject(3, rs.getObject("contest_end_timestamp"));
                update.setObject(4, rs.getObject("contest_type_id"));
                update.setObject(5, rs.getObject("contest_type_desc"));
                update.setObject(6, rs.getObject("phase_id"));
                update.setObject(7, rs.getObject("event_id"));
                update.setLong(8, rs.getLong("contest_id"));

                int retVal = update.executeUpdate();

                if (retVal == 0) {
                    //need to insert
                    insert.clearParameters();
                    insert.setLong(1, rs.getLong("contest_id"));
                    insert.setObject(2, rs.getObject("contest_name"));
                    insert.setObject(3, rs.getObject("contest_start_timestamp"));
                    insert.setObject(4, rs.getObject("contest_end_timestamp"));
                    insert.setObject(5, rs.getObject("contest_type_id"));
                    insert.setObject(6, rs.getObject("contest_type_desc"));
                    insert.setObject(7, rs.getObject("phase_id"));
                    insert.setObject(8, rs.getObject("event_id"));

                    insert.executeUpdate();
                }

            }
            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'contest' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(select);
            close(insert);
            close(update);
        }

    }

    public void doLoadContestPrize() throws Exception {
        log.info("load contest prize");
        PreparedStatement select = null;
        PreparedStatement insert = null;
        PreparedStatement update = null;
        ResultSet rs = null;

        try {
            long start = System.currentTimeMillis();
            final String SELECT =
                    "select ucp.user_id, " +
                    "cp.contest_id, " +
                    "cp.prize_type_id, " +
                    "cp.prize_desc as prize_description, " +
                    "cp.place, " +
                    "cp.prize_amount, " +
                    "ucp.payment as prize_payment " +
                    "from user_contest_prize ucp, " +
                    "contest_prize cp, " +
                    "prize_type_lu pt " +
                    "where cp.contest_prize_id = ucp.contest_prize_id " +
                    "and pt.prize_type_id = cp.prize_type_id " +
                    "and (cp.modify_date > ?) ";
            final String INSERT = "insert into user_contest_prize (contest_id, user_id, prize_type_id, " +
                    "prize_description, place, prize_amount, prize_payment) " +
                    "values (?, ?, ?, ?, ?, ?, ?) ";

            final String UPDATE = "update user_contest_prize set prize_type_id = ?,  prize_description = ?, " +
                    "place = ?, prize_amount = ?, prize_payment = ? " +
                    " where contest_id = ? and user_id = ?";

            //load prizes
            select = prepareStatement(SELECT, SOURCE_DB);
            select.setTimestamp(1, fLastLogTime);
            insert = prepareStatement(INSERT, TARGET_DB);
            update = prepareStatement(UPDATE, TARGET_DB);
            rs = select.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                //update record, if 0 rows affected, insert record
                update.clearParameters();
                update.setObject(1, rs.getObject("prize_type_id"));
                update.setObject(2, rs.getObject("prize_description"));
                update.setObject(3, rs.getObject("place"));
                update.setObject(4, rs.getObject("prize_amount"));
                update.setObject(5, rs.getObject("prize_payment"));
                update.setLong(6, rs.getLong("contest_id"));
                update.setLong(7, rs.getLong("user_id"));

                int retVal = update.executeUpdate();

                if (retVal == 0) {
                    insert.clearParameters();
                    insert.setLong(1, rs.getLong("contest_id"));
                    insert.setLong(2, rs.getLong("user_id"));
                    insert.setObject(3, rs.getObject("prize_type_id"));
                    insert.setObject(4, rs.getObject("prize_description"));
                    insert.setObject(5, rs.getObject("place"));
                    insert.setObject(6, rs.getObject("prize_amount"));
                    insert.setObject(7, rs.getObject("prize_payment"));

                    insert.executeUpdate();
                }

            }
            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'user_contest_prize' table failed.\n" +
                    sqle.getMessage());

        } finally {
            close(rs);
            close(select);
            close(insert);
            close(update);
        }
    }


    public void doLoadEvent() throws Exception {
        log.info("load event");
        PreparedStatement select = null;
        PreparedStatement update = null;
        PreparedStatement insert = null;
        ResultSet rs = null;

        try {
            long start = System.currentTimeMillis();
            final String SELECT = "select e.event_name, e.event_id " +
                    "from event e where modify_date > ?";
            final String UPDATE = "update event set event_name = ? " +
                    " where event_id = ? ";

            final String INSERT = "insert into event (event_id, event_name) " +
                    "values (?, ?) ";


            select = prepareStatement(SELECT, SOURCE_DB);
            select.setTimestamp(1, fLastLogTime);
            update = prepareStatement(UPDATE, TARGET_DB);
            insert = prepareStatement(INSERT, TARGET_DB);
            rs = select.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                //log.debug("PROCESSING EVENT " + rs.getInt("event_id"));

                //update record, if 0 rows affected, insert record
                update.clearParameters();
                update.setObject(1, rs.getObject("event_name"));
                update.setLong(2, rs.getLong("event_id"));

                int retVal = update.executeUpdate();

                if (retVal == 0) {
                    //need to insert
                    insert.clearParameters();
                    insert.setLong(1, rs.getLong("event_id"));
                    insert.setObject(2, rs.getObject("event_name"));

                    insert.executeUpdate();
                }

            }
            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'events' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(select);
            close(insert);
            close(update);
        }
    }


    public void doLoadUserEvent() throws Exception {
        log.info("load user event");
        PreparedStatement select = null;
        PreparedStatement update = null;
        PreparedStatement insert = null;
        ResultSet rs = null;

        try {

            long start = System.currentTimeMillis();
            final String SELECT = "select create_date, event_id, user_id " +
                    "from user_event_xref where modify_date > ?";
            final String UPDATE = "update user_event_xref set create_date = ? " +
                    " where event_id = ? and user_id = ?";

            final String INSERT = "insert into user_event_xref (event_id, user_id, create_date) " +
                    "values (?, ?, ?) ";

            select = prepareStatement(SELECT, SOURCE_DB);
            select.setTimestamp(1, fLastLogTime);
            update = prepareStatement(UPDATE, TARGET_DB);
            insert = prepareStatement(INSERT, TARGET_DB);
            rs = select.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                //log.debug("PROCESSING EVENT " + rs.getInt("event_id"));

                update.clearParameters();
                update.setObject(1, rs.getObject("create_date"));
                update.setLong(2, rs.getLong("event_id"));
                update.setLong(3, rs.getLong("user_id"));

                int retVal = update.executeUpdate();

                if (retVal == 0) {
                    //need to insert
                    insert.clearParameters();
                    insert.setLong(1, rs.getLong("event_id"));
                    insert.setLong(2, rs.getLong("user_id"));
                    insert.setObject(3, rs.getObject("create_date"));

                    insert.executeUpdate();
                }
            }
            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'user_event_xref' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(select);
            close(insert);
            close(update);
        }
    }


    /**
     * Get a sorted list (by rating desc) of all the active coders
     * and their ratings.
     * @return List containing CoderRating objects
     * @throws Exception if something goes wrong when querying
     */
    private List getCurrentRatings() throws Exception {
        StringBuffer query;
        PreparedStatement psSel = null;
        ResultSet rs = null;
        List ret = null;

        try {

            query = new StringBuffer(100);


            query.append(" select ur.user_id");
            query.append(" , ur.rating");
            query.append(" , ur.phase_id");
            query.append(" , case");
            query.append(" when ur.phase_id = 113 and exists (select '1' from active_developers adev where adev.user_id = ur.user_id)");
            query.append(" then 1 else 0 end as active_dev");
            query.append(" , case");
            query.append(" when ur.phase_id = 112 and exists (select '1' from active_designers ades where ades.user_id = ur.user_id)");
            query.append(" then 1 else 0 end as active_des");
            query.append(" , cs.school_id");
            query.append(" , c.coder_type_id");
            query.append(" , c.comp_country_code");
            query.append(" from user_rating ur");
            query.append(" , outer current_school cs");
            query.append(" , coder c");
            query.append(" where ur.user_id = cs.coder_id");
            query.append(" and ur.user_id = c.coder_id");
            query.append(" and c.status = 'A'");

            psSel = prepareStatement(query.toString(), TARGET_DB);

            rs = psSel.executeQuery();
            ret = new ArrayList();
            while (rs.next()) {
                //pros
                if (rs.getInt("coder_type_id") == 2) {
                    if (rs.getInt("phase_id") == 113) {
                        ret.add(new CoderRating(rs.getLong("user_id"), rs.getInt("rating"),
                                rs.getInt("active_dev") == 1, rs.getInt("phase_id"), rs.getString("comp_country_code")));
                    } else {
                        ret.add(new CoderRating(rs.getLong("user_id"), rs.getInt("rating"),
                                rs.getInt("active_des") == 1, rs.getInt("phase_id"), rs.getString("comp_country_code")));
                    }
                } else {
                    //students
                    if (rs.getInt("phase_id") == 113) {
                        ret.add(new CoderRating(rs.getLong("user_id"), rs.getInt("rating"), rs.getLong("school_id"),
                                rs.getInt("active_dev") == 1, rs.getInt("phase_id"), rs.getString("comp_country_code")));
                    } else {
                        ret.add(new CoderRating(rs.getLong("user_id"), rs.getInt("rating"), rs.getLong("school_id"),
                                rs.getInt("active_des") == 1, rs.getInt("phase_id"), rs.getString("comp_country_code")));
                    }
                }
            }


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Getting list of ratings failed.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(psSel);
        }
        return ret;

    }

    private class CoderRating implements Comparable {
        private long coderId = 0;
        private int rating = 0;
        private long schoolId = 0;
        private boolean active = false;
        private int phaseId = 0;
        private String countryCode = null;

        CoderRating(long coderId, int rating, long schoolId, boolean active, int phaseId, String countryCode) {
            this.coderId = coderId;
            this.rating = rating;
            this.schoolId = schoolId;
            this.active = active;
            this.phaseId = phaseId;
            this.countryCode = countryCode;
        }

        CoderRating(long coderId, int rating, boolean active, int phaseId, String countryCode) {
            this.coderId = coderId;
            this.rating = rating;
            this.active = active;
            this.phaseId = phaseId;
            this.countryCode = countryCode;
        }

        public int compareTo(Object other) {
            if (((CoderRating) other).getRating() > rating)
                return 1;
            else if (((CoderRating) other).getRating() < rating)
                return -1;
            else
                return 0;
        }

        long getCoderId() {
            return coderId;
        }

        int getRating() {
            return rating;
        }

        void setCoderId(long coderId) {
            this.coderId = coderId;
        }

        void setRating(int rating) {
            this.rating = rating;
        }

        long getSchoolId() {
            return schoolId;
        }

        void setSchoolId(long schoolId) {
            this.schoolId = schoolId;
        }

        boolean isActive() {
            return active;
        }

        void setActive(boolean active) {
            this.active = active;
        }

        int getPhaseId() {
            return phaseId;
        }

        void setPhaseId(int phaseId) {
            this.phaseId = phaseId;
        }

        String getCountryCode() {
            return countryCode;
        }

        void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String toString() {
            return coderId + ":" + rating + ":" + schoolId + ":" + active + ":" + phaseId;
        }
    }

    private void doLoadRank(int phaseId, int rankTypeId, List list) throws Exception {
        log.info("load rank");
        StringBuffer query = null;
        PreparedStatement psDel = null;
        PreparedStatement psSel = null;
        PreparedStatement psIns = null;
        ResultSet rs = null;
        int count = 0;
        int coderCount = 0;

        try {

            long start = System.currentTimeMillis();
            query = new StringBuffer(100);
            query.append(" DELETE");
            query.append(" FROM user_rank");
            query.append(" WHERE phase_id = " + phaseId);
            query.append("  AND user_rank_type_id = " + rankTypeId);
            psDel = prepareStatement(query.toString(), TARGET_DB);

            query = new StringBuffer(100);
            query.append(" INSERT");
            query.append(" INTO user_rank (user_id, percentile, rank, phase_id, user_rank_type_id)");
            query.append(" VALUES (?, ?, ?, " + phaseId + ", " + rankTypeId + ")");
            psIns = prepareStatement(query.toString(), TARGET_DB);

            /* coder_rank table should be kept "up-to-date" so get the most recent stuff
             * from the rating table
             */
            ArrayList ratings = new ArrayList(list.size() / 2);
            CoderRating cr = null;
            for (int i = 0; i < list.size(); i++) {
                cr = (CoderRating) list.get(i);
                if (cr.getPhaseId() == phaseId) {
                    if ((rankTypeId == ACTIVE_RATING_RANK_TYPE_ID && cr.isActive()) ||
                            rankTypeId != ACTIVE_RATING_RANK_TYPE_ID) {
                        ratings.add(cr);
                    }
                }
            }
            Collections.sort(ratings);

            coderCount = ratings.size();

            psDel.executeUpdate();

            int i = 0;
            int rating = 0;
            int rank = 0;
            int size = ratings.size();
            int tempRating;
            long tempCoderId;
            for (int j = 0; j < size; j++) {
                i++;
                tempRating = ((CoderRating) ratings.get(j)).getRating();
                tempCoderId = ((CoderRating) ratings.get(j)).getCoderId();
                if (tempRating != rating) {
                    rating = tempRating;
                    rank = i;
                }
                psIns.setLong(1, tempCoderId);
                psIns.setFloat(2, (float) 100 * ((float) (coderCount - rank) / coderCount));
                psIns.setInt(3, rank);
                count += psIns.executeUpdate();
            }
            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'user_rank' table failed for rating rank.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(psSel);
            close(psIns);
            close(psDel);
        }

    }

    /**
     * Loads the school_coder_rank table with information about
     * rating rank within a school.
     */
    private void loadSchoolRatingRank(int phaseId, int rankTypeId, List list) throws Exception {
        log.debug("loadSchoolRatingRank called...");
        StringBuffer query = null;
        PreparedStatement psDel = null;
        PreparedStatement psIns = null;
        ResultSet rs = null;
        int count = 0;
        int coderCount = 0;
        List ratings = null;

        try {
            long start = System.currentTimeMillis();
            query = new StringBuffer(100);
            query.append(" DELETE");
            query.append(" FROM school_user_rank");
            query.append(" WHERE user_rank_type_id = " + rankTypeId);
            query.append(" and phase_id = " + phaseId);
            psDel = prepareStatement(query.toString(), TARGET_DB);

            query = new StringBuffer(100);
            query.append(" INSERT");
            query.append(" INTO school_user_rank (user_id, percentile, rank, rank_no_tie, school_id, user_rank_type_id, phase_id)");
            query.append(" VALUES (?, ?, ?, ?, ?, ?, ?)");
            psIns = prepareStatement(query.toString(), TARGET_DB);


            // delete all the records from the country ranking table
            psDel.executeUpdate();

            HashMap schools = new HashMap();
            Long tempId;
            List tempList;
            CoderRating temp;
            /**
             * iterate through our big list and pluck out only those where:
             * the phase lines up
             * they have a school
             * and their status lines up
             */
            for (int i = 0; i < list.size(); i++) {
                temp = (CoderRating) list.get(i);
                if (phaseId == temp.getPhaseId() && temp.getSchoolId() > 0) {
                    if ((rankTypeId == ACTIVE_RATING_RANK_TYPE_ID && temp.isActive()) ||
                        rankTypeId != ACTIVE_RATING_RANK_TYPE_ID) {
                        tempId = new Long(temp.getSchoolId());
                        if (schools.containsKey(tempId)) {
                            tempList = (List) schools.get(tempId);
                        } else {
                            tempList = new ArrayList(10);
                        }
                        tempList.add(list.get(i));
                        schools.put(tempId, tempList);
                        tempList = null;
                    }
                }
            }

            for (Iterator it = schools.entrySet().iterator(); it.hasNext();) {
                ratings = (List) ((Map.Entry) it.next()).getValue();
                Collections.sort(ratings);
                coderCount = ratings.size();

                int i = 0;
                int rating = 0;
                int rank = 0;
                int size = ratings.size();
                int tempRating = 0;
                long tempCoderId = 0;
                for (int j = 0; j < size; j++) {
                    i++;
                    tempRating = ((CoderRating) ratings.get(j)).getRating();
                    tempCoderId = ((CoderRating) ratings.get(j)).getCoderId();
                    if (tempRating != rating) {
                        rating = tempRating;
                        rank = i;
                    }
                    psIns.setLong(1, tempCoderId);
                    psIns.setFloat(2, (float) 100 * ((float) (coderCount - rank) / coderCount));
                    psIns.setInt(3, rank);
                    psIns.setInt(4, j + 1);
                    psIns.setLong(5, ((CoderRating) ratings.get(j)).getSchoolId());
                    psIns.setInt(6, rankTypeId);
                    psIns.setInt(7, ((CoderRating) ratings.get(j)).getPhaseId());
                    count += psIns.executeUpdate();
                }
            }
            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'school_coder_rank' table failed for school coder rating rank.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(psIns);
            close(psDel);
        }

    }

    private void loadCountryRatingRank(int phaseId, int rankTypeId, List list) throws Exception {
        log.debug("loadCountryRatingRank called...");
        StringBuffer query = null;
        PreparedStatement psDel = null;
        PreparedStatement psIns = null;
        ResultSet rs = null;
        int count = 0;
        int coderCount;
        List ratings;

        try {
            long start = System.currentTimeMillis();
            query = new StringBuffer(100);
            query.append(" DELETE");
            query.append(" FROM country_user_rank");
            query.append(" WHERE user_rank_type_id = " + rankTypeId);
            query.append(" and phase_id = " + phaseId);
            psDel = prepareStatement(query.toString(), TARGET_DB);

            query = new StringBuffer(100);
            query.append(" INSERT");
            query.append(" INTO country_user_rank (user_id, percentile, rank, rank_no_tie, user_rank_type_id, phase_id, country_code)");
            query.append(" VALUES (?, ?, ?, ?, ?, ?, ?)");
            psIns = prepareStatement(query.toString(), TARGET_DB);


            // delete all the records from the country ranking table
            psDel.executeUpdate();

            HashMap countries = new HashMap();
            String tempCode = null;
            List tempList = null;
            CoderRating temp = null;
            /**
             * iterate through our big list and pluck out only those where:
             * the phase lines up
             * they have a school
             * and their status lines up
             */
            for (int i = 0; i < list.size(); i++) {
                temp = (CoderRating) list.get(i);
                if (temp.getPhaseId() == phaseId) {
                    if ((rankTypeId == ACTIVE_RATING_RANK_TYPE_ID && temp.isActive()) ||
                            rankTypeId != ACTIVE_RATING_RANK_TYPE_ID) {
                        tempCode = temp.getCountryCode();
                        if (countries.containsKey(tempCode)) {
                            tempList = (List) countries.get(tempCode);
                        } else {
                            tempList = new ArrayList(100);
                        }
                        tempList.add(list.get(i));
                        countries.put(tempCode, tempList);
                        tempList = null;
                    }
                }
            }

            for (Iterator it = countries.entrySet().iterator(); it.hasNext();) {
                ratings = (List) ((Map.Entry) it.next()).getValue();
                Collections.sort(ratings);
                coderCount = ratings.size();

                int i = 0;
                int rating = 0;
                int rank = 0;
                int size = ratings.size();
                int tempRating;
                long tempCoderId;
                for (int j = 0; j < size; j++) {
                    i++;
                    tempRating = ((CoderRating) ratings.get(j)).getRating();
                    tempCoderId = ((CoderRating) ratings.get(j)).getCoderId();
                    if (tempRating != rating) {
                        rating = tempRating;
                        rank = i;
                    }
                    psIns.setLong(1, tempCoderId);
                    psIns.setFloat(2, (float) 100 * ((float) (coderCount - rank) / coderCount));
                    psIns.setInt(3, rank);
                    psIns.setInt(4, j + 1);
                    psIns.setInt(5, rankTypeId);
                    psIns.setInt(6, ((CoderRating) ratings.get(j)).getPhaseId());
                    psIns.setString(7, ((CoderRating) ratings.get(j)).getCountryCode());
                    count += psIns.executeUpdate();
                }
            }
            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'school_coder_rank' table failed for school coder rating rank.\n" +
                    sqle.getMessage());
        } finally {
            close(rs);
            close(psIns);
            close(psDel);
        }

    }


    public void doLoadScorecardTemplate() throws Exception {
        log.info("load scorecard template");
        ResultSet rs = null;

        PreparedStatement select = null;
        PreparedStatement update = null;
        PreparedStatement insert = null;


        final String SELECT = "select template_id as scorecard_template_id, " +
                                           "scorecard_type as scorecard_type_id, " +
                                           "template_name as scorecard_type_desc  " +
                                           "from scorecard_template where modify_date > ?";


        final String UPDATE =
                "update scorecard_template set scorecard_type_id=?, scorecard_type_desc=? where scorecard_template_id = ?";

        final String INSERT =
                "insert into scorecard_template (scorecard_type_id, scorecard_type_desc, scorecard_template_id) values (?, ?, ?)";


        try {
            long start = System.currentTimeMillis();

            select = prepareStatement(SELECT, SOURCE_DB);
            select.setTimestamp(1, fLastLogTime);
            update = prepareStatement(UPDATE, TARGET_DB);
            insert = prepareStatement(INSERT, TARGET_DB);

            int count = 0;

            rs = select.executeQuery();

            while (rs.next()) {

                update.clearParameters();

                update.setObject(1, rs.getObject("scorecard_type_id"));
                update.setObject(2, rs.getObject("scorecard_type_desc"));
                update.setLong(3, rs.getLong("scorecard_template_id"));

                int retVal = update.executeUpdate();

                if (retVal == 0) {
                    insert.clearParameters();

                    insert.setObject(1, rs.getObject("scorecard_type_id"));
                    insert.setObject(2, rs.getObject("scorecard_type_desc"));
                    insert.setLong(3, rs.getLong("scorecard_template_id"));

                    insert.executeUpdate();
                }
                count++;

            }

            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'scorecard_template' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(insert);
            close(update);
            close(select);
        }
    }



    public void doLoadEvaluationLU() throws Exception {
        log.info("load evaluation_lu");
        ResultSet rs;

        PreparedStatement select = null;
        PreparedStatement update = null;
        PreparedStatement insert = null;


        final String SELECT = "select evaluation_id, " +
                           "evaluation_name as evaluation_desc, " +
                           "value as evaluation_value, " +
                           "evaluation_type_id, " +
                           "(select eval_type_name from evaluation_type where evaluation_type_id = e.evaluation_type_id) as evaluation_type_desc " +
                            "from evaluation e ";

        final String UPDATE =
                "update evaluation_lu set evaluation_desc=?,evaluation_value=?,evaluation_type_id=?, evaluation_type_desc=? where evaluation_id = ?";

        final String INSERT =
                "insert into evaluation_lu (evaluation_desc, evaluation_value, evaluation_type_id,evaluation_type_desc,  evaluation_id) values (?, ?, ?, ?, ?)";


        try {
            long start = System.currentTimeMillis();

            select = prepareStatement(SELECT, SOURCE_DB);
            update = prepareStatement(UPDATE, TARGET_DB);
            insert = prepareStatement(INSERT, TARGET_DB);

            int count = 0;

            rs = select.executeQuery();

            while (rs.next()) {

                update.clearParameters();

                update.setObject(1, rs.getObject("evaluation_desc"));
                update.setObject(2, rs.getObject("evaluation_value"));
                update.setObject(3, rs.getObject("evaluation_type_id"));
                update.setObject(4, rs.getObject("evaluation_type_desc"));
                update.setLong(5, rs.getLong("evaluation_id"));

                int retVal = update.executeUpdate();

                if (retVal == 0) {
                    insert.clearParameters();

                    insert.setObject(1, rs.getObject("evaluation_desc"));
                    insert.setObject(2, rs.getObject("evaluation_value"));
                    insert.setObject(3, rs.getObject("evaluation_type_id"));
                    insert.setObject(4, rs.getObject("evaluation_type_desc"));
                    insert.setLong(5, rs.getLong("evaluation_id"));

                    insert.executeUpdate();
                }
                count++;

            }

            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'evaluation_lu' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(insert);
            close(update);
            close(select);
        }
    }



    public void doLoadScorecardQuestion() throws Exception {
        log.info("load scorecard_question");
        ResultSet rs;

        PreparedStatement select = null;
        PreparedStatement update = null;
        PreparedStatement insert = null;


        final String SELECT = "select  qt.q_template_v_id as scorecard_question_id, " +
                "qt.template_id as scorecard_template_id, " +
                "qt.question_text, " +
                "qt.question_weight, " +
                "qt.section_id, " +
                "ss.section_name as section_desc, " +
                "ss.section_weight, " +
                "ss.group_id as section_group_id, " +
                "sg.group_name as section_group_desc,  " +
                "round (sg.group_seq_loc) || \".\" || round(ss.section_seq_loc) || \".\" || round(qt.question_seq_loc)  as question_desc,  " +
                "sg.group_seq_loc, ss.section_seq_loc, qt.question_seq_loc  "+
                "from question_template qt, scorecard_section ss, sc_section_group sg  " +
                "where qt.cur_version = 1  " +
                "and ss.section_id = qt.section_id  " +
                "and sg.group_id = ss.group_id  " +
                //we can't make this join because then if we load before a review fills out a new scorecard, we'll never get the new questions over
                //"and exists (select q_template_v_id from scorecard_question where qt.q_template_v_id = q_template_v_id) " +
                "and (qt.modify_date > ?) " +
                "order by scorecard_template_id, sg.group_seq_loc, ss.section_seq_loc, qt.question_seq_loc  ";



        final String UPDATE =
                 "update scorecard_question set scorecard_template_id=?, question_text=?,question_weight=?, section_id=?,section_desc=?, " +
                 "section_weight=?, section_group_id=?, section_group_desc=?, question_desc=?, sort=?, question_header = ? " +
                 "where scorecard_question_id = ?";

        final String INSERT =
                "insert into scorecard_question (scorecard_template_id, question_text,question_weight, section_id, section_desc, "+
                "section_weight, section_group_id, section_group_desc, question_desc, sort, question_header, scorecard_question_id)" +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


        long questionId = 0;
        try {
            long start = System.currentTimeMillis();

            select = prepareStatement(SELECT, SOURCE_DB);
            select.setTimestamp(1, fLastLogTime);
            update = prepareStatement(UPDATE, TARGET_DB);
            insert = prepareStatement(INSERT, TARGET_DB);

            int count = 0;

            rs = select.executeQuery();

            long prevTemplate = -1;
            int sort=0;

            while (rs.next()) {

                if (rs.getLong("scorecard_template_id") != prevTemplate) {
                    sort = 0;
                    prevTemplate = rs.getLong("scorecard_template_id");
                } else {
                    sort++;
                }

                String questionHeader = (String)rs.getObject("question_text");
                if (questionHeader != null) {
/*                    int p1 = questionHeader.indexOf(". ");
                    int p2 = questionHeader.indexOf('\n');

                    int posic = questionHeader.length();
                    if (p1 >= 0 && p1 < posic) posic = p1 + 1;
                    if (p2 >= 0 && p2 < posic) posic = p2;

                    questionHeader = questionHeader.substring(0, posic);
                    */
                    int posic = questionHeader.indexOf('\n');
                    if (posic >= 0) {
                        questionHeader = questionHeader.substring(0, posic);
                    }

                }

                update.clearParameters();

                questionId = rs.getLong("scorecard_template_id");

                update.setLong(1, questionId);
                update.setObject(2, rs.getObject("question_text"));
                update.setObject(3, rs.getObject("question_weight"));
                update.setObject(4, rs.getObject("section_id"));
                update.setObject(5, rs.getObject("section_desc"));
                update.setObject(6, rs.getObject("section_weight"));
                update.setObject(7, rs.getObject("section_group_id"));
                update.setObject(8, rs.getObject("section_group_desc"));
                update.setObject(9, rs.getObject("question_desc"));
                update.setInt   (10, sort);
                update.setObject(11, questionHeader);
                update.setLong  (12, rs.getLong("scorecard_question_id"));

                int retVal = update.executeUpdate();

                if (retVal == 0) {
                    insert.clearParameters();

                    insert.setObject(1, rs.getObject("scorecard_template_id"));
                    insert.setObject(2, rs.getObject("question_text"));
                    insert.setObject(3, rs.getObject("question_weight"));
                    insert.setObject(4, rs.getObject("section_id"));
                    insert.setObject(5, rs.getObject("section_desc"));
                    insert.setObject(6, rs.getObject("section_weight"));
                    insert.setObject(7, rs.getObject("section_group_id"));
                    insert.setObject(8, rs.getObject("section_group_desc"));
                    insert.setObject(9, rs.getObject("question_desc"));
                    insert.setInt   (10, sort);
                    insert.setObject(11, questionHeader);
                    insert.setLong  (12, rs.getLong("scorecard_question_id"));

                    insert.executeUpdate();
                }
                count++;

            }

            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'scorecard_question' table failed on " + questionId + ".\n" +
                    sqle.getMessage());
        } finally {
            close(insert);
            close(update);
            close(select);
        }
    }


    public void doLoadScorecardResponse() throws Exception {
        log.info("load scorecard_response");
        ResultSet rs = null;

        PreparedStatement select = null;
        PreparedStatement update = null;
        PreparedStatement insert = null;
        ResultSet projects = null;
        PreparedStatement projectSelect;


        final String SELECT = "select sq.q_template_v_id  as scorecard_question_id, " +
//"select (select q_template_v_id from question_template qt where qt.q_template_v_id = sq.q_template_v_id and cur_version=1) as scorecard_question_id, " +
                       "s.scorecard_id, " +
                      "(select submitter_id from submission where submission_id = s.submission_id and cur_version=1) as user_id, " +
                      "s.author_id as reviewer_id, " +
                      "s.project_id, " +
                      "sq.evaluation_id " +
                      "from scorecard_question sq, scorecard s " +
                      "where s.scorecard_id = sq.scorecard_id " +
                      "and s.cur_version = 1 " +
                      "and sq.cur_version = 1 " +
                      "and project_id = ?" +
                      "and (sq.modify_date > ? OR s.modify_date > ?)";

        final String UPDATE =
                "update scorecard_response set user_id=?, reviewer_id=?, project_id=?, evaluation_id=? where scorecard_question_id = ? and scorecard_id = ?";

        final String INSERT =
                "insert into scorecard_response (user_id, reviewer_id, project_id, evaluation_id, scorecard_question_id, scorecard_id) values (?, ?, ?, ?, ?, ?)";

        long questionId = 0;
        try {
            long start = System.currentTimeMillis();

            select = prepareStatement(SELECT, SOURCE_DB);
            update = prepareStatement(UPDATE, TARGET_DB);
            insert = prepareStatement(INSERT, TARGET_DB);
            projectSelect = prepareStatement(PROJECT_SELECT, SOURCE_DB);

            projects = projectSelect.executeQuery();

            int count = 0;

            while (projects.next()) {
                select.clearParameters();
                select.setLong(1, projects.getLong("project_id"));
                select.setTimestamp(2, fLastLogTime);
                select.setTimestamp(3, fLastLogTime);

                rs = select.executeQuery();

                while (rs.next()) {
                    update.clearParameters();
                    questionId = rs.getLong("scorecard_question_id");

                    update.setObject(1, rs.getObject("user_id"));
                    update.setObject(2, rs.getObject("reviewer_id"));
                    update.setObject(3, rs.getObject("project_id"));
                    update.setObject(4, rs.getObject("evaluation_id"));
                    update.setLong(5, questionId);
                    update.setLong(6, rs.getLong("scorecard_id"));

                    int retVal = update.executeUpdate();

                    if (retVal == 0) {
                        insert.clearParameters();

                        insert.setObject(1, rs.getObject("user_id"));
                        insert.setObject(2, rs.getObject("reviewer_id"));
                        insert.setObject(3, rs.getObject("project_id"));
                        insert.setObject(4, rs.getObject("evaluation_id"));
                        insert.setLong(5, questionId);
                        insert.setLong(6, rs.getLong("scorecard_id"));

                        insert.executeUpdate();
                    }

                    count++;

                }
                close(rs);
            }

            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'scorecard_response' table failed for question " + questionId + " .\n" +
                    sqle.getMessage());
        } finally {
            close(projects);
            close(insert);
            close(update);
            close(select);
        }
    }

    public void doLoadTestcaseResponse() throws Exception {
        log.info("load testcase_response");
        ResultSet rs;

        PreparedStatement select = null;
        PreparedStatement update = null;
        PreparedStatement insert = null;


        final String SELECT =
                      //"select (select q_template_id from question_template qt where qt.q_template_v_id = sq.q_template_v_id and cur_version=1)  as scorecard_question_id, " +
                      "select sq.q_template_v_id as scorecard_question_id, " +
                      "       s.scorecard_id, " +
                      "       (select submitter_id from submission where submission_id = s.submission_id and cur_version=1) as user_id, " +
                      "       s.author_id as reviewer_id, " +
                      "       s.project_id, " +
                      "       tq.total_tests as num_tests, " +
                      "       tq.total_pass as num_passed " +
                      "from testcase_question tq, scorecard s, scorecard_question sq " +
                      "where s.scorecard_id = sq.scorecard_id " +
                      "and sq.question_id = tq.question_id " +
                      "and sq.cur_version = 1 " +
                      "and s.cur_version = 1  " +
                      "and tq.cur_version = 1 " +
                      "and (tq.modify_date > ? OR sq.modify_date > ? OR s.modify_date > ?)";

        final String UPDATE =
                "update testcase_response set user_id=?, reviewer_id=?, project_id=?, num_tests=?, num_passed=? where scorecard_question_id = ? and scorecard_id = ?";

        final String INSERT =
                "insert into testcase_response (user_id, reviewer_id, project_id, num_tests, num_passed, scorecard_question_id, scorecard_id) values (?, ?, ?, ?, ?, ?, ?)";


        try {
            long start = System.currentTimeMillis();

            select = prepareStatement(SELECT, SOURCE_DB);
            select.setTimestamp(1, fLastLogTime);
            select.setTimestamp(2, fLastLogTime);
            select.setTimestamp(3, fLastLogTime);
            update = prepareStatement(UPDATE, TARGET_DB);
            insert = prepareStatement(INSERT, TARGET_DB);

            int count = 0;

            rs = select.executeQuery();

            while (rs.next())  {

                update.clearParameters();

                update.setObject(1, rs.getObject("user_id"));
                update.setObject(2, rs.getObject("reviewer_id"));
                update.setObject(3, rs.getObject("project_id"));
                update.setObject(4, rs.getObject("num_tests"));
                update.setObject(5, rs.getObject("num_passed"));
                update.setLong(6, rs.getLong("scorecard_question_id"));
                update.setLong(7, rs.getLong("scorecard_id"));

                int retVal = update.executeUpdate();

                if (retVal == 0) {
                    insert.clearParameters();

                    insert.setObject(1, rs.getObject("user_id"));
                    insert.setObject(2, rs.getObject("reviewer_id"));
                    insert.setObject(3, rs.getObject("project_id"));
                    insert.setObject(4, rs.getObject("num_tests"));
                    insert.setObject(5, rs.getObject("num_passed"));
                    insert.setLong(6, rs.getLong("scorecard_question_id"));
                    insert.setLong(7, rs.getLong("scorecard_id"));

                    insert.executeUpdate();
                }
                count++;

            }

            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'testcase_response' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(insert);
            close(update);
            close(select);
        }
    }



    public void doLoadSubjectiveResponse() throws Exception {
        log.info("load subjective_response");
        ResultSet rs;

        PreparedStatement select = null;
        PreparedStatement update = null;
        PreparedStatement insert = null;
        ResultSet projects;
        PreparedStatement projectSelect;


        final String SELECT =
                       //"select (select q_template_id from question_template qt where qt.q_template_v_id = sq.q_template_v_id and cur_version=1) as scorecard_question_id, " +
                        "select sq.q_template_v_id as scorecard_question_id, " +
                        "s.scorecard_id, " +
                        "(select submitter_id from submission where submission_id = s.submission_id and cur_version=1) as user_id, " +
                        "s.author_id as reviewer_id, " +
                        "s.project_id, " +
                        "sr.response_text, " +
                        "sr.response_type_id, " +
                        "(select response_type_name from response_type where sr.response_type_id = response_type_id) as response_type_desc, " +
                        " subjective_resp_id " +
                        "from subjective_resp sr, scorecard s, scorecard_question sq " +
                        "where s.scorecard_id = sq.scorecard_id " +
                        "and sq.question_id = sr.question_id " +
                        "and s.cur_version = 1 " +
                        "and sr.cur_version = 1 " +
                        "and sq.cur_version = 1 "+
                        "and project_id = ? " +
                        "and (sr.modify_date>? OR s.modify_date>? OR sq.modify_date>?) " +
                        "order by scorecard_question_id, scorecard_id, subjective_resp_id";
        final String UPDATE =
                "update subjective_response set user_id=?, reviewer_id=?, project_id=?, response_text=?, response_type_id=?,response_type_desc=? where  sort=? and scorecard_question_id = ? and scorecard_id = ?";

        final String INSERT =
                "insert into subjective_response (user_id, reviewer_id, project_id, response_text, response_type_id, response_type_desc, sort, scorecard_question_id, scorecard_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";


        try {
            long start = System.currentTimeMillis();

            select = prepareStatement(SELECT, SOURCE_DB);
            update = prepareStatement(UPDATE, TARGET_DB);
            insert = prepareStatement(INSERT, TARGET_DB);
            projectSelect = prepareStatement(PROJECT_SELECT, SOURCE_DB);

            int count = 0;

            long prevScorecardQuestion = -1;
            long prevScorecard = -1;
            int sort=0;


            projects = projectSelect.executeQuery();

            while (projects.next()) {
                select.clearParameters();
                select.setLong(1, projects.getLong("project_id"));
                select.setTimestamp(2, fLastLogTime);
                select.setTimestamp(3, fLastLogTime);
                select.setTimestamp(4, fLastLogTime);

                rs = select.executeQuery();

                while (rs.next()) {
                    if ((rs.getLong("scorecard_question_id") != prevScorecardQuestion) || (rs.getLong("scorecard_id") != prevScorecard)) {
                        sort = 0;
                        prevScorecardQuestion = rs.getLong("scorecard_question_id");
                        prevScorecard = rs.getLong("scorecard_id");
                    } else {
                        sort++;
                    }


                    update.clearParameters();

                    update.setObject(1, rs.getObject("user_id"));
                    update.setObject(2, rs.getObject("reviewer_id"));
                    update.setObject(3, rs.getObject("project_id"));
                    update.setObject(4, rs.getObject("response_text"));
                    update.setObject(5, rs.getObject("response_type_id"));
                    update.setObject(6, rs.getObject("response_type_desc"));
                    update.setInt(7, sort);
                    update.setLong(8, rs.getLong("scorecard_question_id"));
                    update.setLong(9, rs.getLong("scorecard_id"));

                    int retVal = update.executeUpdate();

                    if (retVal == 0) {
                        insert.clearParameters();

                        insert.setObject(1, rs.getObject("user_id"));
                        insert.setObject(2, rs.getObject("reviewer_id"));
                        insert.setObject(3, rs.getObject("project_id"));
                        insert.setObject(4, rs.getObject("response_text"));
                        insert.setObject(5, rs.getObject("response_type_id"));
                        insert.setObject(6, rs.getObject("response_type_desc"));
                        insert.setInt(7, sort);
                        insert.setLong(8, rs.getLong("scorecard_question_id"));
                        insert.setLong(9, rs.getLong("scorecard_id"));

                        insert.executeUpdate();
                    }
                    count++;

                }
            }

            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'subjective_response' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(insert);
            close(update);
            close(select);
        }
    }


    public void doLoadAppeal() throws Exception {
        log.info("load Appeal");
        ResultSet rs;

        PreparedStatement select = null;
        PreparedStatement update = null;
        PreparedStatement insert = null;
        ResultSet projects;
        PreparedStatement projectSelect;


        final String SELECT = "select a.appeal_id, " +
                            "sq.q_template_v_id as scorecard_question_id, " +
                            "s.scorecard_id, " +
                            "(select submitter_id from submission where submission_id = s.submission_id and cur_version=1) as user_id, " +
                            "s.author_id as reviewer_id, " +
                            "s.project_id, " +
                            "sq.evaluation_id as final_evaluation_id, " +
                            "a. appeal_text, " +
                            "a.appeal_response, " +
                            "a.raw_evaluation_id " +
                            "from appeal a, scorecard s, scorecard_question sq " +
                            "where s.scorecard_id = sq.scorecard_id " +
                            "and sq.question_id = a.question_id " +
                            "and sq.cur_version = 1 " +
                            "and s.cur_version = 1  " +
                            "and a.cur_version = 1 " +
                            "and a.is_resolved = 1 " +
                            "and sq.evaluation_id is not null " +
                            "and project_id = ? " +
                            "and (a.modify_date>? OR s.modify_date>? OR sq.modify_date>?)";

        final String UPDATE =
                "update appeal set scorecard_question_id = ?, scorecard_id = ?, user_id=?, reviewer_id=?, project_id=?, " +
                "raw_evaluation_id=?, final_evaluation_id=?, appeal_text=?, appeal_response=? where appeal_id=?";

        final String INSERT =
                "insert into appeal (scorecard_question_id, scorecard_id, user_id, reviewer_id, project_id, " +
                "raw_evaluation_id, final_evaluation_id, appeal_text, appeal_response, appeal_id) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


        try {
            long start = System.currentTimeMillis();

            select = prepareStatement(SELECT, SOURCE_DB);
            update = prepareStatement(UPDATE, TARGET_DB);
            insert = prepareStatement(INSERT, TARGET_DB);
            projectSelect = prepareStatement(PROJECT_SELECT, SOURCE_DB);

            int count = 0;

            projects = projectSelect.executeQuery();

            while (projects.next()) {
                select.clearParameters();
                select.setLong(1, projects.getLong("project_id"));
                select.setTimestamp(2, fLastLogTime);
                select.setTimestamp(3, fLastLogTime);
                select.setTimestamp(4, fLastLogTime);

                rs = select.executeQuery();

                while (rs.next()) {

                    update.clearParameters();

                    update.setLong(1, rs.getLong("scorecard_question_id"));
                    update.setLong(2, rs.getLong("scorecard_id"));
                    update.setObject(3, rs.getObject("user_id"));
                    update.setObject(4, rs.getObject("reviewer_id"));
                    update.setObject(5, rs.getObject("project_id"));
                    update.setObject(6,rs.getObject("raw_evaluation_id"));
                    update.setObject(7, rs.getObject("final_evaluation_id"));
                    update.setObject(8, rs.getObject("appeal_text"));
                    update.setObject(9, rs.getObject("appeal_response"));
                    update.setLong(10, rs.getLong("appeal_id"));

                    int retVal = update.executeUpdate();

                    if (retVal == 0) {
                        insert.clearParameters();

                        insert.setLong(1, rs.getLong("scorecard_question_id"));
                        insert.setLong(2, rs.getLong("scorecard_id"));
                        insert.setObject(3, rs.getObject("user_id"));
                        insert.setObject(4, rs.getObject("reviewer_id"));
                        insert.setObject(5, rs.getObject("project_id"));
                        insert.setObject(6, rs.getObject("raw_evaluation_id"));
                        insert.setObject(7, rs.getObject("final_evaluation_id"));
                        insert.setObject(8, rs.getObject("appeal_text"));
                        insert.setObject(9, rs.getObject("appeal_response"));
                        insert.setLong(10, rs.getLong("appeal_id"));

                        insert.executeUpdate();
                    }
                    count++;

                }
            }

            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'appeal' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(insert);
            close(update);
            close(select);
        }
    }

    public void doLoadTestcaseAppeal() throws Exception {
        log.info("load Testcase Appeal");
        ResultSet rs;

        PreparedStatement select = null;
        PreparedStatement update = null;
        PreparedStatement insert = null;
        ResultSet projects;
        PreparedStatement projectSelect;


        final String SELECT = "select a.appeal_id, "+
                            "sq.q_template_v_id as scorecard_question_id, "+
                            "s.scorecard_id, "+
                            "(select submitter_id from submission where submission_id = s.submission_id and cur_version=1) as user_id, "+
                            "s.author_id as reviewer_id, "+
                            "s.project_id, "+
                            "tq.total_tests as final_num_tests, "+
                            "tq.total_pass as final_num_passed, "+
                            "a.appeal_text, "+
                            "a.appeal_response, " +
                            "a.raw_total_tests, " +
                            "a.raw_total_pass " +
                            "from appeal a, scorecard s, testcase_question tq, scorecard_question sq "+
                            "where   s.scorecard_id = sq.scorecard_id  "+
                            "and sq.question_id = tq.question_id  "+
                            "and tq.question_id = a.question_id "+
                            "and tq.cur_version = 1 "+
                            "and s.cur_version = 1  "+
                            "and a.cur_version = 1 "+
                            "and a.is_resolved = 1 "+
                            "and project_id = ? " +
                            "and (a.modify_date>? OR s.modify_date>? OR tq.modify_date>? OR sq.modify_date>?)";


        final String UPDATE =
                "update testcase_appeal set scorecard_question_id = ?, scorecard_id = ?, user_id=?, reviewer_id=?, project_id=?, " +
                "raw_num_passed=?, raw_num_tests=?, final_num_passed=?, final_num_tests=?, appeal_text=?, appeal_response=? where appeal_id=?";

        final String INSERT =
                "insert into testcase_appeal (scorecard_question_id, scorecard_id, user_id, reviewer_id, project_id, " +
                "raw_num_passed, raw_num_tests, final_num_passed, final_num_tests, appeal_text, appeal_response, appeal_id) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            long start = System.currentTimeMillis();

            select = prepareStatement(SELECT, SOURCE_DB);
            update = prepareStatement(UPDATE, TARGET_DB);
            insert = prepareStatement(INSERT, TARGET_DB);
            projectSelect = prepareStatement(PROJECT_SELECT, SOURCE_DB);

            int count = 0;

            projects = projectSelect.executeQuery();

            while (projects.next()) {
                select.clearParameters();
                select.setLong(1, projects.getLong("project_id"));
                select.setTimestamp(2, fLastLogTime);
                select.setTimestamp(3, fLastLogTime);
                select.setTimestamp(4, fLastLogTime);
                select.setTimestamp(5, fLastLogTime);

                rs = select.executeQuery();

                while (rs.next()) {

                    update.clearParameters();

                    update.setLong(1, rs.getLong("scorecard_question_id"));
                    update.setLong(2, rs.getLong("scorecard_id"));
                    update.setObject(3, rs.getObject("user_id"));
                    update.setObject(4, rs.getObject("reviewer_id"));
                    update.setObject(5, rs.getObject("project_id"));
                    update.setObject(6, rs.getObject("raw_total_pass"));
                    update.setObject(7, rs.getObject("raw_total_tests"));
                    update.setObject(8, rs.getObject("final_num_passed"));
                    update.setObject(9, rs.getObject("final_num_tests"));
                    update.setObject(10, rs.getObject("appeal_text"));
                    update.setObject(11, rs.getObject("appeal_response"));
                    update.setLong(12, rs.getLong("appeal_id"));

                    int retVal = update.executeUpdate();

                    if (retVal == 0) {
                        insert.clearParameters();

                        insert.setLong(1, rs.getLong("scorecard_question_id"));
                        insert.setLong(2, rs.getLong("scorecard_id"));
                        insert.setObject(3, rs.getObject("user_id"));
                        insert.setObject(4, rs.getObject("reviewer_id"));
                        insert.setObject(5, rs.getObject("project_id"));
                        insert.setObject(6, rs.getObject("raw_total_pass"));
                        insert.setObject(7, rs.getObject("raw_total_tests"));
                        insert.setObject(8, rs.getObject("final_num_passed"));
                        insert.setObject(9, rs.getObject("final_num_tests"));
                        insert.setObject(10, rs.getObject("appeal_text"));
                        insert.setObject(11, rs.getObject("appeal_response"));
                        insert.setLong(12, rs.getLong("appeal_id"));

                        insert.executeUpdate();
                    }
                    count++;

                }
            }

            log.info("loaded " + count + " records in " + (System.currentTimeMillis() - start) / 1000 + " seconds");


        } catch (SQLException sqle) {
            DBMS.printSqlException(true, sqle);
            throw new Exception("Load of 'testcase_appeal' table failed.\n" +
                    sqle.getMessage());
        } finally {
            close(insert);
            close(update);
            close(select);
        }
    }


}
