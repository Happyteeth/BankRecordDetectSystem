package com.cfss.rules;

public class RuleStaticInfo {

    public final static String RULE_RUN_TASK_ID = "RULE_RUN_";

    //规则提交到队列表（RULE_RUN）中的状态
    public final static int RULE_STATUS_SUBMIT = 1;
    //规则被调度，准备执行的状态
    public final static int RULE_STATUS_RUNNING = 2;
    //规则执行完成
    public final static int RULE_STATUS_FINISH = 8;
    //规则执行异常
    public final static int RULE_STATUS_EXCEPTION = 9;
    //此规则上次已经执行，本次不再执行
    public final static int RULE_STATUS_NO_NEED_RUN = 0;
    public final static String PARA_ID_RULE_RUN_TIMEOUT_DEFAULT = "7200";

    //RULE_RUN参数在《程序参数表》中的程序ID
    public final static String PROG_ID_RULE_RUN = "RULE_RUN";
    //RULE_RUN参数在《程序参数表》中对应的具体的参数ID。 数据SQL保存的地址
    public final static String PARA_ID_DATA_SQL_PATH = "DATA_SQL_PATH";
    // 数据SQL在HDFS上的暂存地址
    public final static String PARA_ID_DATA_SQL_HDFS_PATH = "DATA_SQL_HDFS_PATH";
    // 数据SQL执行结果的前N行保存在SQL中，这个N对应的参数ID
    public final static String PARA_ID_DATA_SQL_ROWS = "DATA_SQL_ROWS";
    public final static int PARA_ID_DATA_SQL_ROWS_DEFALUT = 100;

    //图形SQL执行结果超过N行后，后面的数据会被截断
    public final static String PATA_ID_CHART_SQL_ROW= "CHART_SQL_ROWS";
    public final static int PATA_ID_CHART_SQL_ROW_DEFAULT  = 10000;
    //规则执行的超时时间，如果某个规则的执行时间超过此值，监控线程会将它杀死，并将执行的状态设置为异常状态。
    public final static String PARA_ID_RULE_RUN_TIMEOUT = "RULE_RUN_TIMEOUT";

    //默认的分割符
    public final static String PARA_ID_SPLIT_CHARATER = "SPLIT_CHARATER";
    public final static String PARA_ID_SPLIT_CHARATER_DEFAULT = "|";

    //数据SQL能够同时启动的线程
    public final static String PARA_ID_THREAD_NUM = "THREAD_NUM";

    //每次的间隔
    public final static String PARA_ID_SCAN_TASK_INTERVAL = "SCAN_TASK_INTERVAL";



}
