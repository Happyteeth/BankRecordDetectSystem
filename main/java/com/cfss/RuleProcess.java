package com.cfss ;

import java.io.File;
import java.sql.*;

import cn.hutool.core.util.StrUtil;
import com.cfss.rules.RuleRun;
import com.cfss.rules.RuleStaticInfo;
import com.cfss.util.DateUtil;
import com.cfss.util.JDBCUtil;
import com.cfss.util.SQLStaicInfo;
import com.cfss.util.TaskRunLogUtil;
import com.cfss.util.process.local.LocalExecutor;
//import com.cfss.util.process.spark.SparkTaskLauncher;
import com.cfss.util.tasklog.CLevel;
import com.cfss.util.tasklog.CLogLevel;
import com.ylink.aml.core.common.constant.Const;
import com.ylink.aml.modular.system.util.ReadConfigUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * 用于规则/模型的运行
 */
public class RuleProcess implements Runnable {
    public static final Log LOG = LogFactory.getLog(RuleProcess.class.getName());

//    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-ddHHmmss");

    /**
     * 入口函数。由定时线程调度
     */
    public void run() {
        LOG.info("start run RuleProcess....");
        getNeedToRunRule(null);

    }

    public void runSignle( String runRunId ) {
        LOG.info("start run RuleProcess....runRunId="+ runRunId);
        getNeedToRunRule(runRunId);
    }

    /**
     * 从数据表中获取需要跑的数据表
     */
    public void getNeedToRunRule(String runRunId) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            conn = JDBCUtil.getConnection();
            if(StrUtil.isBlank(runRunId)) {
            statement = conn.prepareStatement(SQLStaicInfo.SQL_RULE_NEED_TO_RUN);
            }else{
                // 指定运行那个一个
                statement = conn.prepareStatement(SQLStaicInfo.SQL_RULE_NEED_TO_SINGLE_RUN);
                statement.setString(1,runRunId);
            }

            RuleRun ruleRun =  null; ;
            while((ruleRun = getRuleRunFromRuleRun(statement)) != null){
                LOG.info("Success to get one rule from  RULE_RUN. rule_run_id = " + ruleRun.getRuleRunId() + ", rule_id = " + ruleRun.getRuleRunId() );
                //1. 首先锁定该ID，保证同一个线程处理同一个规则。

                    if (!updateRuleStatus(ruleRun.getRuleRunId(), conn)) {
                    //如果更新失败，表明已经有线程处理这条记录，此处只需要跳过即可
                    LOG.info("Fail to update the value of STATUS  in the table: RULE_RUN. rule_run_id = " + ruleRun.getRuleRunId()  + ", rule_id = " + ruleRun.getRuleRunId()+ ". Maybe this rule has been executed by other thread. In this thread, this rule will be ignored.");
                    continue;
                }

                //2. 获取各种时间
                //2.1 获取这一rule_id对应的update_time时间（在RULE_DEF）
                Timestamp ruleUpdateTime = this.getRuleDefUpdateTime(conn, ruleRun.getRuleId());
                //2.2 获取对应的rule_id最后一次执行状态为（2或者8）提交时间
                RuleRun lasRuleRun = this.getLastFinishRuleRun(conn, ruleRun.getRuleId(), ruleRun.getRuleRunId());
                Timestamp lastsubmitTime = null;
                if(lasRuleRun != null){
                    lastsubmitTime= lasRuleRun.getSubmitTime();
                }
                //2.3 获取模型参数表中的参数修改时间
                Timestamp lastParaTime = this.getRuleParaMaxTime(conn, ruleRun.getRuleId());
                //2.4 获取对应表的参数修改时间
                Timestamp lastTargetTime = this.getTargetMaxTime(conn, ruleRun.getRuleId());
                if(lastTargetTime==null){
                    lastTargetTime = new Timestamp(System.currentTimeMillis());
                }

                //2.5 对比这些时间，如果此规则之前已经提交过，而且没有修改，则不用执行.
                if (!this.compareTime(lastsubmitTime, ruleUpdateTime, lastTargetTime, lastParaTime)) {
                    //执行update命令即可
//                    TaskRunLogUtil.saveLogWithConn(conn, RuleStaticInfo.RULE_RUN_TASK_ID + ruleBean.getRuleFileName(), CLevel.INFO,CLogLevel.PROCESS_LOG_DETAIL_LEVEL_1,
//                            "This rule has been success to execute in " + DateUtil.getTimeStampofString(submitRunTime) + ". No need to run again.  RuleId = " + ruleId + " submitTime = " + DateUtil.getTimeStampofString(submitTime),
//                            "", "");
                    dealWithNoNeedRunRule(ruleRun.getRuleRunId(), lasRuleRun, conn);
                    continue;
                }

                //3. 开始执行规则
//                this.executeSparkRule(ruleRun);
                if(!this.executeLocalRule(ruleRun, conn)){
                    this.updateRuleRunDb(ruleRun, conn, RuleStaticInfo.RULE_STATUS_EXCEPTION);
                    JDBCUtil.updateRuleRunDb(ruleRun, conn, RuleStaticInfo.RULE_STATUS_EXCEPTION);
                }


//                if(this.executeLocalRule(ruleBean, conn)){
//                    this.updateRuleRunDb(ruleBean, conn, RuleStaticInfo.RULE_STATUS_FINISH);
//                }else{
//                    this.updateRuleRunDb(ruleBean, conn, RuleStaticInfo.RULE_STATUS_EXCEPTION);
//                }
//
            }
        } catch (SQLException e) {
            LOG.error(e);
//            e.printStackTrace();
        } finally {
            JDBCUtil.closeResources(conn, statement, resultSet);
        }
        LOG.info("Finish to execute..... ");
    }


    /**
     * 从RULE_RUN表中获取一条需要执行的记录。
     * @param statement
     * @return
     */
    private  RuleRun getRuleRunFromRuleRun( PreparedStatement statement){
        ResultSet resultSet = null;
        RuleRun ruleRun = null;
        try{
//            "SELECT RULE_RUN_ID, RULE_ID, MIN(SUBMIT_TIME) AS SUBMIT_TIME, TASK_PROGRAM, RULE_PROG, CHART_PROG, STATUS FROM RULE_RUN WHERE RULE_RUN.STATUS =
            resultSet = statement.executeQuery();

            if(!resultSet.next()){
                System.out.println("Fail to get rule_run....");
                return null;
            }
            String ruleRunId = resultSet.getString(1);
            if(ruleRunId == null){
                //ruleRunId为RULE_RUN的关键字，肯定不会为空.
                return null;
            }
            String ruleId =  resultSet.getString(2);
            Timestamp sumbTime = resultSet.getTimestamp(3);
            String taskProgram = resultSet.getString(4);
            String ruleProg = resultSet.getString(5);
            String chartProg = resultSet.getString(6);
            ruleRun = new RuleRun(ruleRunId, ruleId, sumbTime, taskProgram, ruleProg, chartProg);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            JDBCUtil.closeResources(null, null, resultSet);
        }
        return ruleRun;
    }


    private String getLocalResultFolder(Connection conn){
        return "";
    }

    private boolean updateRuleStatus(RuleRun rule, Connection conn) {

        return true;
    }

    private boolean  executeLocalRule(RuleRun rule, Connection conn) {
        try {
            LOG.info("Begin to execute rule: " + rule.getRuleId() + " ,runid=" + rule.getRuleRunId() + ". submit time = " + rule.getSubmitTimeStr());
            TaskRunLogUtil.saveLogWithConn(conn, RuleStaticInfo.RULE_RUN_TASK_ID + rule.getRuleRunId(), CLevel.INFO, CLogLevel.PROCESS_LOG,
                    "In RuleProcess. Begin to execute rule. rule_run_id = " + rule.getRuleRunId(), "", rule.getRuleProg());
            String localPath = JDBCUtil.getValueFromMapping(conn, RuleStaticInfo.PROG_ID_RULE_RUN, RuleStaticInfo.PARA_ID_DATA_SQL_PATH, null); //这一个是对应参数表中某个参数
            File fileDir = new File(localPath);
            //如果文件夹不存在，则创建新的文件夹
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            if (localPath == null) {
                LOG.error("Fail to get parameter value of DATA_SQL_PATH. Please check the table : TBS_PROG_PARA ");
                return false;
            }
            int n = getIntValueFromTable(conn, RuleStaticInfo.PARA_ID_DATA_SQL_ROWS, RuleStaticInfo.PARA_ID_DATA_SQL_ROWS_DEFALUT);
            int chartMax = getIntValueFromTable(conn, RuleStaticInfo.PARA_ID_DATA_SQL_ROWS, RuleStaticInfo.PATA_ID_CHART_SQL_ROW_DEFAULT);
            String splitCharater = JDBCUtil.getValueFromMapping(conn, RuleStaticInfo.PROG_ID_RULE_RUN, RuleStaticInfo.PARA_ID_SPLIT_CHARATER, RuleStaticInfo.PARA_ID_SPLIT_CHARATER_DEFAULT);
            Object appType= ReadConfigUtil.getCommonYml("aml.appType", "application.yml");
            if(Const.APP_TYPE_PAY.equalsIgnoreCase(appType.toString())){
                return LocalExecutor.executeRule(rule, conn, localPath, n, chartMax, splitCharater, true);
            }else{
                return LocalExecutor.executeRule(rule, conn, localPath, n, chartMax, splitCharater);
            }
        }catch (Exception e){
            LOG.error("executeLocalRule throw", e);
        }
        return false;
    }

    private int getIntValueFromTable (Connection conn, String paraId , int defaultValue){
        int n = defaultValue;
        try{
            n = Integer.parseInt(JDBCUtil.getValueFromMapping(conn, RuleStaticInfo.PROG_ID_RULE_RUN, paraId, null));
        }catch (Exception e){
            LOG.warn("Exception exit. The default value  -- " + defaultValue + "  will be used. " + e);
            n = defaultValue;
        }
        return n;
    }




    /**
     * 需要运行执行的规则或模型
     *
     * @return
     */
    private boolean executeSparkRule(RuleRun rule) {
        //对于不同的任务类型进行判断，
//        int topN = this.getIntValueFromTable(conn, RuleStaticInfo.PARA_ID_DATA_SQL_ROWS, RuleStaticInfo.PARA_ID_DATA_SQL_ROWS_DEFALUT);
//        int charMax = this.getIntValueFromTable(conn, RuleStaticInfo.PATA_ID_CHART_SQL_ROW, RuleStaticInfo.PATA_ID_CHART_SQL_ROW_DEFAULT);
//        String hdfs = JDBCUtil.getValueFromMapping(conn, RuleStaticInfo.PROG_ID_RULE_RUN, RuleStaticInfo.PARA_ID_DATA_SQL_HDFS_PATH, null);
//        String localPath = JDBCUtil.getValueFromMapping(conn, RuleStaticInfo.PROG_ID_RULE_RUN, RuleStaticInfo.PARA_ID_DATA_SQL_PATH, null);
//        String splitCharater = JDBCUtil.getValueFromMapping(conn, RuleStaticInfo.PROG_ID_RULE_RUN, RuleStaticInfo.PARA_ID_SPLIT_CHARATER, RuleStaticInfo.PARA_ID_SPLIT_CHARATER_DEFAULT);
//         SparkTaskLauncher.executeSpark(rule);
   //      SparkTaskLauncher.executeSpark(rule);
         return true;
//        return SparkExecutor.executeSparkSql(rule, topN,localPath, hdfs,  charMax, splitCharater, conn);
    }

    /**
     * 最后更新一下结果数据库
     *
     * @param rule
     * @param conn
     * @return
     */
    private boolean updateRuleRunDb(RuleRun rule, Connection conn, int status) {
        PreparedStatement statement = null;
        try {
            //"UPDATE RULE_RUN SET STATUS = ? , RUN_TIME = ?, RERULT_LINE_N = ?, RERULT_CHART_DATA = ?, RERULT_PATH = ?, D_UPDATE = ?  WHERE RULE_RUN_ID = ?";
            statement = conn.prepareStatement(SQLStaicInfo.SQL_UPDATE_RULE_RUN_FINAL);
            statement.setInt(1, status);
            statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            statement.setString(3, rule.getResultLineN().toString());
            statement.setString(4,rule.getResultChartData().toString());
            statement.setString(5, rule.getResultPath());

            statement.setLong(6, rule.getResultCount());
            statement.setTimestamp(7,new Timestamp(System.currentTimeMillis()));
            statement.setString(8, rule.getRuleRunId());
            statement.executeUpdate();

        } catch (Exception e) {
            LOG.error("Fail to update RULE_RUN. Exception :" + e );
            return false;
        } finally {
            JDBCUtil.closeResources(statement, null);
        }
        return true;

    }


    /**
     * 用于更新运行规则的状态,将当前的提交状态修改为运行状态
     * 需要注意,这里的更新需要状态是否更新成功，如果成功，则表明当前线程可以继续处理
     * 这个记录，如果更新失败，则可能是当前这条记录已经被其它线程处理过，当前线程应该跳过这条记录，去处理其它
     * 记录。
     *
     * @param ruleRunId     : rule_run_id 是RULE_RUN的主键
     * @param conn       ：数据库的连接
     * @return
     */
    private boolean updateRuleStatus(String ruleRunId,  Connection conn) {

        LOG.info("Begin to update  ruleRunId = " + ruleRunId +  " . status to " + RuleStaticInfo.RULE_STATUS_RUNNING);
        PreparedStatement statement = null;
        int ret = 0;
        try {
            statement = conn.prepareStatement(SQLStaicInfo.SQL_UPDATE_RULE_RUN_STATUS);
            statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            statement.setString(2, ruleRunId);
            ret = statement.executeUpdate();
            LOG.info("Success to update . ret = " + ret);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JDBCUtil.closeResources(statement, null);
        }
        if (ret == 0) {
            return false;
        }
        return true;
    }


    /**
     * 用于处理不需要继续执行的任务。
     * 只有一种情况：上一次的运行结束时间是在所有的修改时间之后（sql的修改时间，目标表的修改， 参数的修改时间）
     *
     * @param ruleRunId
     * @param lastRuleRun
     * @param conn
     * @return
     */
    private boolean dealWithNoNeedRunRule(String ruleRunId, RuleRun lastRuleRun, Connection conn) {

        PreparedStatement statement = null;
        int ret = 0;
        try {
            statement = conn.prepareStatement(SQLStaicInfo.SQL_UPDATE_RULE_RUN_NO_RUN);
//            public static final String SQL_UPDATE_RULE_RUN_NO_RUN = "UPDATE RULE_RUN SET STATUS = " + RuleStaticInfo.RULE_STATUS_NO_NEED_RUN + ", RULE_RUN_ID_RESULT = ? , D_UPDATE = ? WHERE RULE_RUN_ID = ?";
            statement.setString(1, lastRuleRun.getRuleRunId());
            statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            statement.setString(3, ruleRunId);
            ret = statement.executeUpdate();
        } catch (SQLException e) {
            LOG.error("Fail to update RULE_RUN. Exception : " + e);
        } finally {
            JDBCUtil.closeResources(statement, null);
        }

        if (ret != 1) {
            LOG.info("Fail to update table -- rule_run. ruleRunId = " + ruleRunId );
            return false;
        }
        LOG.info("Success to   update table -- rule_run. ruleRunId = " + ruleRunId + ". the result_run_id = " + lastRuleRun.getRuleRunId());

        return true;
    }


    /**
     * 用于比较参数时间与规则最后一次执行成功的提交时间。
     * 如果修改时间在运行时间之前，则表明不需要重新运行，则返回false, 否则表明该条规则需要重新执行，返回true
     * 如果运行时间为空，则直接返回true
     *
     * @param submitTime    ： 规则最后一次执行成功的运行时间
     * @param sqlTime    ：规则最后一次修改的更新时间
     * @param targetTime ： 规则对应的数据表的最后一次的更新时间
     * @param paraTime   ： 模型对应的参数修改时间（如果为空，则不比较这个时间）
     * @return
     */
    private boolean compareTime(Timestamp submitTime, Timestamp sqlTime, Timestamp targetTime, Timestamp paraTime) {

        LOG.info(this.processStr(submitTime, sqlTime, targetTime, paraTime));

        long sub = DateUtil.convertTimeStamp2Long(submitTime);
        long st = DateUtil.convertTimeStamp2Long(sqlTime);
        long tt = DateUtil.convertTimeStamp2Long(targetTime);
        long pt = DateUtil.convertTimeStamp2Long(paraTime);

        long maxTime = getMaxValue(st, tt, pt);
        if(sub > maxTime){
            return false;
        }
        return true;

//        if (submitTime == null) {
//            return true;
//        }
//        //首先获取sqlTime与targetTime的最大时间
//        Timestamp time1 = this.getMax(sqlTime, targetTime);
//        //再次获取time1与paraTime中最大的时间
//        Timestamp time2 = this.getMax(time1, paraTime);
//        if (time2 == null) {
//            //如果为空，则重新跑一次吧
//            return true;
//        }
//
//        if (submitTime.after(time2)) {
//
//            return true;
//        }
//        return false;
    }

    private long getMaxValue(long t1, long t2, long t3){
        long t4 = t1 > t2 ? t1: t2;
        return t3 > t4 ? t3: t4;
    }



    private String processStr(Timestamp runTime, Timestamp sqlTime, Timestamp targetTime, Timestamp paraTime) {
        StringBuilder sb = new StringBuilder();

        sb.append("Begin to compare time.... . Submit time for the last successful execution  = ").append(DateUtil.getTimeStampofString(runTime));
        sb.append(". Last modify rule time = " ).append(DateUtil.getTimeStampofString(sqlTime));
        sb.append(". Last modify target file time = ").append(DateUtil.getTimeStampofString(targetTime));
        sb.append(". Last modify parameter of rules time = ").append(DateUtil.getTimeStampofString(paraTime));
        return sb.toString();
    }



    private Timestamp getMax(Timestamp time1, Timestamp time2) {
        if (time1 == null) {
            return time2;
        }
        if (time2 == null) {
            return time1;
        }

        if (time1.after(time2)) {
            return time1;
        }
        return time2;
    }


    private RuleRun getLastFinishRuleRun(Connection conn, String ruleId, String curRuleRunId){
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        RuleRun ruleRun = null;
        try{
            statement = conn.prepareStatement(SQLStaicInfo.SQL_MAX_TIME_RULE_RUN_TIME);
            statement.setString(1, ruleId);
            statement.setString(2, curRuleRunId);
            resultSet = statement.executeQuery();
            //只取第一条记录
            if(resultSet.next()) {
    //            "SELECT RULE_RUN_ID, RULE_ID, MAX(SUBMIT_TIME)
                    String ruleRunId = resultSet.getString(1);
                    Timestamp submitTime = resultSet.getTimestamp(2);
                    ruleRun = new RuleRun(ruleRunId);
                    ruleRun.setSubmitTime(submitTime);
            }

        }catch (Exception e){
            LOG.error("Fail to execute sql. " + e);
        } finally {
            JDBCUtil.closeResources(null, statement, resultSet);
        }
        return ruleRun;
    }

    /**
     * 获取
     *
     * @param conn
     * @return
     */
    private Timestamp getTargetMaxTime(Connection conn, String ruleId) {
        return this.getMaxTimeFromTable(conn, SQLStaicInfo.SQL_MAX_TIME_TARGET_TAB, ruleId);
    }

    private Timestamp getRuleRunMaxTime(Connection conn, String ruleId) {
        return this.getMaxTimeFromTable(conn, SQLStaicInfo.SQL_MAX_TIME_RULE_RUN_TIME, ruleId);
    }

    private Timestamp getRuleParaMaxTime(Connection conn, String ruleId) {
        return this.getMaxTimeFromTable(conn, SQLStaicInfo.SQL_MAX_TIME_RULE_PARA_DEF, ruleId);
    }

    private Timestamp getRuleDefUpdateTime(Connection conn, String ruleId){
        return this.getMaxTimeFromTable(conn, SQLStaicInfo.SQL_MAX_TIME_RULE_DEF, ruleId);
    }


    //根据SQL，获取最大的UPDATE_TIME
    private Timestamp getMaxTimeFromTable(Connection conn, String sql, String ruleId) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Timestamp result = null;
        try {
            statement = conn.prepareStatement(sql);
            statement.setString(1, ruleId);
            //因为SQL已经确定了取最大的，因此我们只需要取一个值即可
            resultSet = statement.executeQuery();
            if(resultSet.next()){
                result = resultSet.getTimestamp(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            LOG.error("Exception throws . " + e);
        } finally {
            JDBCUtil.closeResources(statement, resultSet);
        }
        return result;
    }


    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir", "D:\\hadoop");//加载hadoop组件
        RuleProcess ru = new RuleProcess();
        ru.getNeedToRunRule(null);
    }


}
