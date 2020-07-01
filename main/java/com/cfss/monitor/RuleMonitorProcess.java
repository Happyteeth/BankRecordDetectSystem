package com.cfss.monitor;

import com.cfss.rules.RuleRun;
import com.cfss.rules.RuleStaticInfo;
import com.cfss.util.DateUtil;
import com.cfss.util.JDBCUtil;
import com.cfss.util.TaskRunLogUtil;
import com.cfss.util.tasklog.CLevel;
import com.cfss.util.tasklog.CLogLevel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * 用于监控任务执行状态，如果超过时间限制之后，会执行两个动作：
 * 1. 将原有的进程杀掉
 * 2. 将状态信息改为异常
 *
 * 此线程不是常驻线程，它会每隔一段时间由调度线程调度执行
 */
public class RuleMonitorProcess  implements  Runnable{
    public static final Log LOG = LogFactory.getLog(RuleMonitorProcess.class.getName());
    private static final String SQL = "SELECT RULE_RUN_ID, RULE_ID, APP_ID, D_UPDATE FROM RULE_RUN WHERE STATUS = ?";
    //设置的默认超时时间为7200秒
    private long costTime = 7200L;

//    RuleMonitorProcess(){
//        Connection conn = null;
//        try{
//            conn = JDBCUtil.getConnection();
//            this.costTime = this.getExecuteRuleTimeOut(conn);
//        }catch (Exception e){
//            e.printStackTrace();
//            LOG.error(e);
//        }finally {
//            JDBCUtil.closeResources(conn, null, null);
//        }
//    }


    public void run() {
        monitorRule();
    }

    public void monitorRule(){
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try{
            conn = JDBCUtil.getConnection();
            this.costTime = this.getExecuteRuleTimeOut(conn);
            statement = conn.prepareStatement(RuleMonitorProcess.SQL);
            statement.setString(1, String.valueOf(RuleStaticInfo.RULE_STATUS_RUNNING));
            resultSet = statement.executeQuery();
            while (resultSet.next()){
                //RULE_RUN_ID, RULE_ID, APP_ID, D_UPDATE
                String ruleRunId = resultSet.getString(1);
                String ruleId = resultSet.getString(2);
                String appId = resultSet.getString(3);
                Timestamp timestamp = resultSet.getTimestamp(4);
                RuleRun ruleRun = new RuleRun(ruleRunId);
                ruleRun.setRuleId(ruleId);
                ruleRun.setAppId(appId);
                ruleRun.setUpdate(timestamp);

            LOG.info("current time = " + DateUtil.getTimeStampofString(new Timestamp(System.currentTimeMillis())) + ". The rule last update time = " +  DateUtil.getTimeStampofString(timestamp) +  ". rule_run_id = " + ruleRunId + ". ruleId = " + ruleId + ". ");
                if(System.currentTimeMillis() - DateUtil.convertTimeStamp2Long(timestamp) > costTime * 1000){
                    LOG.info("The execute time of this rule  is more than  " + costTime * 1000 + " . last update time = " +  DateUtil.getTimeStampofString(timestamp) +  ". rule_run_id = " + ruleRunId + ". ruleId = " + ruleId + ". ");
//                    ProcessMonitor.killProcess(ruleProg, appId);
                    this.dealTimeOutProcess(ruleRun, conn);
                }
            }
        }catch (Exception e){
            LOG.error("Exeception e." , e );
//            TaskRunLogUtil.saveLogWithConn(conn, );

        }finally {
            JDBCUtil.closeResources(conn, statement, resultSet);
        }
    }

    /**
     * 用于处理超时的任务
     * @param rule
     * @return
     */
    public boolean dealTimeOutProcess(RuleRun rule, Connection conn){
        this.preTimeoutProcess(rule, conn);
        this.killTimeOutProcess(rule, conn);
        this.postTimeOutProcess(rule, conn);
        return true;
    }

    /**
     * 用于杀死任务进程之前的前置动作
     * @param rule
     * @param conn
     * @return
     */
    public boolean preTimeoutProcess(RuleRun rule, Connection conn){
        return true;
    }

    /**
     * 用于将已经异常的任务进程杀掉, 现阶段为空实现
     * @param rule
     * @param conn
     * @return
     */
    public boolean killTimeOutProcess(RuleRun rule, Connection conn){
        return true;
    }

    /**
     * 当任务进程已经杀死之后，需要将该规则的执行状态改为异常状态。
     * @param rule
     * @param conn
     * @return
     */
    public boolean postTimeOutProcess(RuleRun rule, Connection conn){

        JDBCUtil.updateRuleRunPostExecute(rule, conn, String.valueOf(RuleStaticInfo.RULE_STATUS_EXCEPTION));

        TaskRunLogUtil.saveLogWithConn(conn,RuleStaticInfo.RULE_RUN_TASK_ID+ rule.getRuleRunId(), CLevel.INFO, CLogLevel.PROCESS_LOG_DETAIL_LEVEL_1,"The execute time is more than " + this.costTime + "s. The status will be set "+ RuleStaticInfo.RULE_STATUS_EXCEPTION +" directly. last update time = " + DateUtil.getTimeStampofString(rule.getUpdate()) + ". rule_run_id = " + rule.getRuleRunId(), "", "");


        return true;
    }




   // PARA_ID_RULE_RUN_TIMEOUT_DEFAULT
    public long getExecuteRuleTimeOut(Connection conn){
        String  value = JDBCUtil.getValueFromMapping(conn, RuleStaticInfo.PROG_ID_RULE_RUN, RuleStaticInfo.PARA_ID_RULE_RUN_TIMEOUT, RuleStaticInfo.PARA_ID_RULE_RUN_TIMEOUT_DEFAULT);
        long costValue = 0L;
        try{
            costValue = Long.parseLong(value);
        }catch (Exception e){
            e.printStackTrace();
            costValue = Long.parseLong(RuleStaticInfo.PARA_ID_RULE_RUN_TIMEOUT_DEFAULT);
        }
        System.out.println("costValue = " + costValue);
        return costValue;
    }

    public static void main(String[] args) {
        RuleMonitorProcess ruleMonitorProcess = new RuleMonitorProcess();
        ruleMonitorProcess.run();
    }


}
