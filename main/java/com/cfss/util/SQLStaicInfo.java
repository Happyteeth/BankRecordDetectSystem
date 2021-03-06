package com.cfss.util;

import com.cfss.rules.RuleStaticInfo;

/**
 * 用于存储所有的SQL查询语句。
 */
public class SQLStaicInfo {
    //从RULE_RUN中获取需要 RULE_RUN_ID
//    public static final String SQL_RULE_NEED_TO_RUN = "SELECT RULE_RUN.RULE_ID,RULE_RUN.SUBMIT_TIME,RULE_RUN.TASK_PROGRAM, RULE_RUN.RULE_PROG,RULE_RUN.CHART_PROG ,RULE_DEF.D_UPDATE FROM RULE_RUN INNER JOIN RULE_DEF ON RULE_RUN.RULE_ID = RULE_DEF.RULE_ID AND STATUS =  " + RuleStaticInfo.RULE_STATUS_SUBMIT;
    //Rule_run表中取出状态为1， 且是最早提交的规则。
    public static final String SQL_RULE_NEED_TO_RUN = "SELECT RULE_RUN_ID, RULE_ID, SUBMIT_TIME , TASK_PROGRAM, RULE_PROG, CHART_PROG, STATUS FROM RULE_RUN WHERE RULE_RUN.STATUS =  "  + RuleStaticInfo.RULE_STATUS_SUBMIT  +" ORDER BY SUBMIT_TIME ASC LIMIT 0,1";


    //获取RULE_COUNT 的SQL语句，从RULE_RUN表中， 现在只支持PAY版本
    public static final String SQL_RULE_COUNT = "SELECT RULE_COUNT FROM RULE_RUN WHERE RULE_RUN_ID = ?";

    //指定那个任务执行
    public static final String SQL_RULE_NEED_TO_SINGLE_RUN =  "SELECT RULE_RUN_ID, RULE_ID, SUBMIT_TIME , TASK_PROGRAM, RULE_PROG, CHART_PROG, STATUS FROM RULE_RUN WHERE RULE_RUN.STATUS =  "  + RuleStaticInfo.RULE_STATUS_SUBMIT+" AND RULE_RUN_ID=?" ;

    public static final String SQL_RULE_NEED_TO_SINGLE_RUN_PAY =  "SELECT RULE_RUN_ID, RULE_ID, SUBMIT_TIME , TASK_PROGRAM, RULE_PROG, CHART_PROG, RULE_COUNT, STATUS FROM RULE_RUN WHERE RULE_RUN.STATUS =  "  + RuleStaticInfo.RULE_STATUS_SUBMIT+" AND RULE_RUN_ID=?" ;

    //根据rulerunid从rule_run中获取基本信息
    public static final String SQL_RULE_RUN_BASED_ON_RULE_RUN_ID = "SELECT  RULE_ID, SUBMIT_TIME, TASK_PROGRAM, RULE_PROG, CHART_PROG, STATUS FROM RULE_RUN WHERE RULE_RUN_ID = ?";

    //从rule_run表中获取对应规则的执行完成最近的提交时间
//    public static final String SQL_MAX_TIME_RULE_RUN_TIME = "SELECT MAX(SUBMIT_TIME),STATUS FROM RULE_RUN WHERE RULE_ID = ? AND STATUS =  " + RuleStaticInfo.RULE_STATUS_FINISH ;
   //从rule_run表中获取对应规则在最后一次执行的基本信息,同时还需要去掉自己的
    public static final String SQL_MAX_TIME_RULE_RUN_TIME = "SELECT RULE_RUN_ID, SUBMIT_TIME FROM RULE_RUN WHERE RULE_ID = ? AND (STATUS = " + RuleStaticInfo.RULE_STATUS_FINISH  + " OR STATUS = " + RuleStaticInfo.RULE_STATUS_RUNNING + ")" + " AND RULE_RUN_ID != ? ORDER BY SUBMIT_TIME DESC limit 0,1" ;

    //从目标表中获取对应的update_time
    public static final String SQL_MAX_TIME_TARGET_TAB = "SELECT MAX(D_UPDATE) FROM DATA_FILE_IMPORT AS A INNER JOIN RULE_TARGET_MAPPING AS B ON A.TABLE_NAME = B.TABLE_NAME WHERE B.RULE_ID = ?";
    //从模型参数表中获取对应的最大的update_time
    public static final String SQL_MAX_TIME_RULE_PARA_DEF = "SELECT MAX(D_UPDATE) FROM RULE_PARA_DEF WHERE RULE_ID = ?";
    //从rule_def中获取对update_time
    public static final String SQL_MAX_TIME_RULE_DEF = "SELECT MAX(D_UPDATE) FROM RULE_DEF WHERE RULE_ID = ?";

    //更新RULE_RUN中的status的值
//    public static final String SQL_UPDATE_RULE_RUN_STATUS = "UPDATE RULE_RUN SET STATUS = " + RuleStaticInfo.RULE_STATUS_RUNNING + ", D_UPDATE = ? " + "  WHERE RULE_ID = ? AND SUBMIT_TIME = ? AND STATUS = " + RuleStaticInfo.RULE_STATUS_SUBMIT;
    public static final String SQL_UPDATE_RULE_RUN_STATUS = "UPDATE RULE_RUN SET STATUS = " + RuleStaticInfo.RULE_STATUS_RUNNING + ", D_UPDATE = ? " + "  WHERE RULE_RUN_ID = ? AND STATUS = " + RuleStaticInfo.RULE_STATUS_SUBMIT;

    public static final String SQL_UPDATE_RULE_RUN_LOCAL_PATH = "UPDATE RULE_RUN SET STATUS = ? , RERULT_PATH = ?,  D_UPDATE = ?  WHERE RULE_RUN_ID = ?";
    public static final String SQL_UPDATE_RULE_RUN_STATUS_RUNNING = "UPDATE RULE_RUN SET STATUS = " + RuleStaticInfo.RULE_STATUS_RUNNING + ", D_UPDATE = ? WHERE RULE_RUN_ID = ? ";


    //不需要重新计算的规则，需要更新RULE_RUN中的参数，首先有status，SUBMIT_TIME_RESULT, RUN_TIME
  //  public static final String SQL_UPDATE_RULE_RUN_NO_RUN = "UPDATE RULE_RUN SET STATUS = " + RuleStaticInfo.RULE_STATUS_FINISH + " , SUBMIT_TIME_RESULT = ?, D_UPDATE = ? WHERE RULE_ID = ? AND SUBMIT_TIME = ?";
    public static final String SQL_UPDATE_RULE_RUN_NO_RUN = "UPDATE RULE_RUN SET STATUS = " + RuleStaticInfo.RULE_STATUS_NO_NEED_RUN + ", RULE_RUN_ID_RESULT = ? , D_UPDATE = ? WHERE RULE_RUN_ID = ?";

    //更新RULE_RUN的值
//    public static final String SQL_UPDATE_RULE_RUN_FINAL = "UPDATE RULE_RUN SET STATUS = ? , RUN_TIME = ?, RERULT_LINE_N = ?, RERULT_CHART_DATA = ?, RERULT_PATH = ?, D_UPDATE = ? WHERE RULE_ID = ? AND SUBMIT_TIME = ? ";
    public static final String SQL_UPDATE_RULE_RUN_FINAL = "UPDATE RULE_RUN SET STATUS = ? , RUN_TIME = ?, RERULT_LINE_N = ?, RERULT_CHART_DATA = ?, RERULT_PATH = ?, RERULT_COUNT = ?, D_UPDATE = ?  WHERE RULE_RUN_ID = ?";


    public static final String  SQL_UPDATE_RULE_CHECK_COUNT = "UPDATE RULE_RUN SET CHECK_COUNT = ? WHERE RULE_RUN_ID = ?";





    //从《程序参数表》（TBS_PROG_PARA）中查询对应的参数值
    public static final String SQL_PARA_VALUE_IN_TBS_PROG_PARA = "SELECT V_PARA_VALUE FROM TBS_PROG_PARA WHERE V_PROG_ID = ? AND V_PARA_ID = ?";

    //从《程序参数表》（TBS_PROG_PARA）查询出程序对应的参数（包含spark等）
    public static final String SQL_QUERY_PARA_VALUE_IN_TBS_PROG_PARA = "SELECT V_PARA_ID, V_PARA_VALUE FROM TBS_PROG_PARA WHERE V_PROG_ID = ?";

//	sb.append("INSERT INTO TBS_TASK_RUN_LOG (V_TASK_ID,TS_DEALDATE,C_LEVEL, C_LOG_LEVEL,V_LOGCONTENT,V_LOGADDS,V_SQL) VALUES ");

    public static final String SQL_INSERT_TASK_LOG = "INSERT INTO TBS_TASK_RUN_LOG (V_TASK_ID,TS_DEALDATE,C_LEVEL, C_LOG_LEVEL,V_LOGCONTENT,V_LOGADDS,V_SQL) VALUES (?,?,?,?,?,?,?)";
}
