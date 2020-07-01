package com.cfss.util.process.local;

import cn.hutool.core.util.StrUtil;
import com.cfss.rules.RuleRun;
import com.cfss.rules.RuleStaticInfo;
import com.cfss.util.*;
import com.cfss.util.tasklog.CLevel;
import com.cfss.util.tasklog.CLogLevel;
import com.ylink.aml.core.common.constant.Const;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.sql.*;

/**
 * 用于执行本地的SQL，本文件原用于小数据版本的规则执行。后期可能会被移走
 */
public class LocalExecutor {
    public static final Log LOG = LogFactory.getLog(LocalExecutor.class.getName());

    public static boolean executeRule(RuleRun rule, Connection conn, String localpath, int topN, int chartMax, String splitCharater){
       return  LocalExecutor.executeRule(rule,conn,localpath,topN,chartMax,splitCharater, false);
    }


    public static boolean executeRule(RuleRun rule, Connection conn, String localpath, int topN, int chartMax, String splitCharater, boolean ispay ) {
        //System.out.println("weeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeecmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmweiucnweucwjciecwjiweciwecjeeeeeeeeeeeeeeeeeeeeeeeee");
        if (!executeRunSql(rule, conn, localpath, topN, splitCharater)) {
            LOG.error("Fail to execute rule sql. rule_run_id = " + rule.getRuleRunId());
            JDBCUtil.updateRuleRunDb(rule, conn, RuleStaticInfo.RULE_STATUS_EXCEPTION);
            TaskRunLogUtil.saveLogWithConn(conn, RuleStaticInfo.RULE_RUN_TASK_ID + rule.getRuleRunId(), CLevel.INFO, CLogLevel.PROCESS_LOG,
                    "In LocalExecutor. Fail to execute rule sql . rule_run_id = " + rule.getRuleRunId(), "", rule.getRuleProg());
            return false;
        }
        //saveLogWithConn(Connection conn, String taskId,String level, String logLevel,String logContent,String logAdds,String vSql){

        TaskRunLogUtil.saveLogWithConn(conn, RuleStaticInfo.RULE_RUN_TASK_ID + rule.getRuleRunId(), CLevel.INFO, CLogLevel.PROCESS_LOG,
                "In LocalExecutor. Success to execute rule sql . rule_run_id = " + rule.getRuleRunId(), "", rule.getRuleProg());

        if (!executeChartSql(rule, conn, chartMax, splitCharater)) {
            LOG.error("Fail to execute chart sql. rule_run_id = " + rule.getRuleRunId());
            JDBCUtil.updateRuleRunDb(rule, conn, RuleStaticInfo.RULE_STATUS_EXCEPTION);
            TaskRunLogUtil.saveLogWithConn(conn, RuleStaticInfo.RULE_RUN_TASK_ID + rule.getRuleRunId(), CLevel.INFO, CLogLevel.PROCESS_LOG,
                    "In LocalExecutor. Fail to execute chart sql . rule_run_id = " + rule.getRuleRunId(), "", rule.getChartProg());
            return false;
        }

        //判断是否为支付版
        if(ispay){
            int cout = LocalExecutor.executeRuleCountSql(rule, conn);
//            rule.setCheckCount(cout);
            updateCheckCountForPay(cout, rule.getRuleRunId(), conn);

        }


        TaskRunLogUtil.saveLogWithConn(conn, RuleStaticInfo.RULE_RUN_TASK_ID + rule.getRuleRunId(), CLevel.INFO, CLogLevel.PROCESS_LOG,
                "In LocalExecutor. Success to execute chart sql . rule_run_id = " + rule.getRuleRunId(), "", rule.getChartProg());

       return  JDBCUtil.updateRuleRunDb(rule, conn, RuleStaticInfo.RULE_STATUS_FINISH);

    }

    public static boolean updateCheckCountForPay(int cout, String ruleRunId, Connection conn){
        PreparedStatement statement = null;
        int ret = 0;
        try{
            statement = conn.prepareStatement(SQLStaicInfo.SQL_UPDATE_RULE_CHECK_COUNT);
            statement.setInt(1, cout);
            statement.setString(2, ruleRunId);
            ret = statement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();

        }finally {
            JDBCUtil.closeResources(statement, null);
        }
        if(ret != 1){
            System.out.println("fail to update....");
            return false;
        }else{
            return true;
        }

    }


    /**
     * 用于执行数据SQL
     *
     * @param rule
     * @param n
     * @return
     */
    public static boolean executeRunSql(RuleRun rule, Connection conn, String localPath, int n, String splitCharater) {

        TaskRunLogUtil.saveLogWithConn(conn, RuleStaticInfo.RULE_RUN_TASK_ID + rule.getRuleRunId(), CLevel.ERROR_STOP,
                CLogLevel.PROCESS_LOG_DETAIL_LEVEL_1, "Begin to execute rule sql. rule_run_id = " + rule.getRuleRunId() + ", rule_id = " + rule.getRuleId()
                , "", rule.getRuleProg());
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String localFile = null;
        if(localPath.endsWith("/"))
            localFile =localPath +   rule.getRuleRunId()+".csv";
        else{
            localFile =localPath + "/"+  rule.getRuleRunId()+".csv";
        }

        try {
            //1.先从数据表中读取数据
            statement = conn.prepareStatement(rule.getRuleProg());
            resultSet = statement.executeQuery();

            parseResultSet(resultSet, rule.getResultLineN(), null, n, splitCharater,true);

            resultSet.beforeFirst();
            //csv 逗号分隔
            long num = parseResultSet(resultSet, null, localFile, n, ",",false);
            rule.setResultCount(num);
            rule.setResultPath(localFile);
        } catch (Exception e) {
            e.printStackTrace();
            TaskRunLogUtil.saveLogWithConn(conn, RuleStaticInfo.RULE_RUN_TASK_ID + rule.getRuleRunId(), CLevel.ERROR_STOP,
                    CLogLevel.PROCESS_LOG_DETAIL_LEVEL_1, "Fail to execute rule . rule_run_id = " + rule.getRuleRunId() + ", rule_id = " + rule.getRuleId()
                    , "", StringUtil.getExceptionToString(e));

        } finally {
            JDBCUtil.closeResources(statement, resultSet);
        }
        TaskRunLogUtil.saveLogWithConn(conn, RuleStaticInfo.RULE_RUN_TASK_ID + rule.getRuleRunId(), CLevel.ERROR_STOP,
                CLogLevel.PROCESS_LOG_DETAIL_LEVEL_1, "Success to execute rule sql. rule_run_id = " + rule.getRuleRunId() + ", rule_id = " + rule.getRuleId()
                , "", rule.getRuleProg());
        return true;
    }

    public static int executeRuleCountSql(RuleRun rule, Connection conn){
       String countsql = getRuleCountSql(rule, conn);
       int cout = 0;
       if(null == countsql || countsql.trim().isEmpty()){
           return cout;
       }

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try{
            statement = conn.prepareStatement(countsql);
            resultSet = statement.executeQuery();
            resultSet.next();
            //如果结果不为数值
            cout = resultSet.getInt(1);


        }catch (Exception e){
            e.printStackTrace();
            TaskRunLogUtil.saveLogWithConn(conn, RuleStaticInfo.RULE_RUN_TASK_ID + rule.getRuleRunId(), CLevel.ERROR_STOP,
                    CLogLevel.PROCESS_LOG_DETAIL_LEVEL_1, "Fail to execute rule count sql . rule_run_id = " + rule.getRuleRunId() + ", rule_id = " + rule.getRuleId()
                    , "", StringUtil.getExceptionToString(e));
        }finally {
            JDBCUtil.closeResources(statement,resultSet);
        }

        return cout;


    }

    private static String getRuleCountSql(RuleRun rule, Connection conn){
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sql = null;

        try {
            //1.先从数据表中读取数据
            statement = conn.prepareStatement(SQLStaicInfo.SQL_RULE_COUNT);
            statement.setString(1,rule.getRuleRunId());
            resultSet = statement.executeQuery();
            resultSet.next();
            sql = resultSet.getString(1);


        } catch (Exception e) {
            e.printStackTrace();
            TaskRunLogUtil.saveLogWithConn(conn, RuleStaticInfo.RULE_RUN_TASK_ID + rule.getRuleRunId(), CLevel.ERROR_STOP,
                    CLogLevel.PROCESS_LOG_DETAIL_LEVEL_1, "Fail to get rule count sql . rule_run_id = " + rule.getRuleRunId() + ", rule_id = " + rule.getRuleId()
                    , "", StringUtil.getExceptionToString(e));

        } finally {
            JDBCUtil.closeResources(statement, resultSet);
        }
        TaskRunLogUtil.saveLogWithConn(conn, RuleStaticInfo.RULE_RUN_TASK_ID + rule.getRuleRunId(), CLevel.ERROR_STOP,
                CLogLevel.PROCESS_LOG_DETAIL_LEVEL_1, "Success to get rule count sql . rule_run_id = " + rule.getRuleRunId() + ", rule_id = " + rule.getRuleId()
                , "", rule.getRuleProg());


        return sql;
    }


    /**
     * 用于执行图表SQL
     *
     * @param rule
     * @param conn
     * @param chartMaxValue
     * @return
     */

    public static boolean executeChartSql(RuleRun rule, Connection conn, int chartMaxValue, String splitCharater) {

        TaskRunLogUtil.saveLogWithConn(conn, RuleStaticInfo.RULE_RUN_TASK_ID + rule.getRuleRunId(), CLevel.ERROR_STOP,
                CLogLevel.PROCESS_LOG_DETAIL_LEVEL_1, "Begin to execute chart sql. rule_run_id = " + rule.getRuleRunId() + ", rule_id = " + rule.getRuleId()
                , "", rule.getRuleProg());

        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            //1.先从数据表中读取数据
            if(StrUtil.length(rule.getChartProg())>5 ) {
                statement = conn.prepareStatement(rule.getChartProg());
                resultSet = statement.executeQuery();
                parseResultSet(resultSet, rule.getResultChartData(), null, chartMaxValue, splitCharater,false);
            }else{
                LOG.info("executeChartSql  is  short，no exceute" );
            }
        } catch (Exception e) {
            LOG.error("executeChartSql expction,", e);
            TaskRunLogUtil.saveLogWithConn(conn, RuleStaticInfo.RULE_RUN_TASK_ID + rule.getRuleRunId(), CLevel.ERROR_STOP,
                    CLogLevel.PROCESS_LOG_DETAIL_LEVEL_1, "Fail to execute rule . rule_run_id = " + rule.getRuleRunId() + ", rule_id = " + rule.getRuleId()
                    , "", StringUtil.getExceptionToString(e));
        } finally {
            JDBCUtil.closeResources(statement, resultSet);
        }
        TaskRunLogUtil.saveLogWithConn(conn, RuleStaticInfo.RULE_RUN_TASK_ID + rule.getRuleRunId(), CLevel.ERROR_STOP,
                CLogLevel.PROCESS_LOG_DETAIL_LEVEL_1, "Success to execute chart sql. rule_run_id = " + rule.getRuleRunId() + ", rule_id = " + rule.getRuleId()
                , "", rule.getRuleProg());
        return true;
    }


    /**
     * @param resultSet     : 需要解析的数据集
     * @param sf            : 前n行保存的地址
     * @param localFile     : 数据的本地路径。如果不需要保存，此处传入为空
     * @param n             :  保存的n行地址
     * @param splitCharater : 数据的分割符
     * @param  onlyFirstBatch : 仅首批
     * @throws SQLException
     */
    public static long parseResultSet(ResultSet resultSet, StringBuffer sf, String localFile, long n, String splitCharater,boolean onlyFirstBatch) throws SQLException {

        FileWriter fw = null;
        long num = 0L;
        try {
            //如果localFile值为null，则表明不保存文件
            if (localFile != null) {
                fw = new FileWriter(localFile);
                 fw.init();
            }
            //1. 从ResultSet中读取
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columns = rsmd.getColumnCount();
            //2. 首先拼出列名
            StringBuffer sb = new StringBuffer();
            for (int i = 1; i <= columns; i++) {
                sb.append(rsmd.getColumnLabel(i));
                if (i != columns) {
                    sb.append(splitCharater);
                }
            }
            sb.append("\r\n");
            //将数据保存到sf中
//            sf.append(sb);
            //3. 准备获取数据


            boolean tag = true;
            while (resultSet.next()) {
                num++;
                for (int j = 1; j <= columns; j++) {
                    Object obj =resultSet.getObject(j);
                    if(obj!=null) {
                        sb.append(obj);
                    }else{
                        sb.append("");
                    }
                    if (j != columns) {
                        sb.append(splitCharater);
                    }
                }
                sb.append("\r\n");
                //4. 判断一下num的行数，每n行则保存一次数据到文件中
                if (num % n == 0) {
                    //数据达到n行的限制，则数据保存到
                    if (tag && sf!=null) {
                        sf.append(sb);
                    }
                    tag = false;
                    if(onlyFirstBatch){
                        break;
                    }
                    write2File(fw, sb.toString());
                    sb = new StringBuffer();
                }
            }


            if (num < n  && sf!=null) {
                sf.append(sb);
            }
            write2File(fw, sb.toString());
        } finally {
            if (fw != null) {
                fw.close();
            }
        }
        return num;
    }

    /**
     * 用于写文件。如果fw为空，则直接退出。因为它可能不需要写数据
     *
     * @param fw
     * @param content
     */
    private static void write2File(FileWriter fw, String content) {
        if (fw == null) {
            return;
        }
        fw.writerFile(content);
    }


    public static void main(String[] args) {

        Connection conn = JDBCUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = conn.prepareStatement("select * from RULE_RUN");
            resultSet = statement.executeQuery();
//            RuleRun rule = new RuleRun("123", new Timestamp(System.currentTimeMillis()));

//            LocalExecutor.executeRule(rule, conn, "d:",100, 100, "|");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtil.closeResources(conn, statement, resultSet);
        }

    }
}
