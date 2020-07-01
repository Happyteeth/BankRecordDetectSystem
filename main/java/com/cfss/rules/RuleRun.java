package com.cfss.rules;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class RuleRun {
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    /**
     * 执行运行ID
     */
    private String ruleRunId;
    /**
     * 规则ID
     */
    private String ruleId;
    /**
     * 提交执行时间
     */
    private Timestamp submitTime;
    /**
     * 结果关联提交执行时间
     */
    private Timestamp submitTimeResult;
    /**
     * 规则检测执行语言： 0 -- SQL， 1 -- Oracle 2 -- Python
     */
    private String taskProgram;
    /**
     * 数据SQL或程序
     */
    private String ruleProg;
    /**
     * 数据SQL执行的结果的行数
     */
    private Long resultCount;

    /**
     * 图表SQL或程序
     */
    private String chartProg;
    /**
     * 状态 1 -- 初始提交；2 -- 运行中 ；8 -- 执行完成；9 -- 执行异常
     */
    private String status;
    /**
     * 执行完成时间
     */
    private Timestamp runTime;
    /**
     * 数据SQL执行结果的前N行，使用标准格式
     */
    private StringBuffer resultLineN;
    /**
     * 执行结果文件路径：包含目录和文件名 文件名：RULE_ID+SUBMIT_TIME
     */
    private String resultPath;
    /**
     * 执行结果图表数据（标准格式）
     */
    private StringBuffer resultChartData;
    /**
     * 如果数据需要存储在hdfs上面，保存在此参数中
     */
    private String hdfsFolder;
    private String ruleCount;
    /**
     * 数据最络拷贝到本地的路径
     */
    private String resultFilePath;
    private long checkCount;
    private String appId;


    /**
     * 操作修改时间
     */
    private Timestamp update;


    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public String getResultFilePath() {
        return resultFilePath;
    }

    public void setResultFilePath(String resultFilePath) {
        this.resultFilePath = resultFilePath;
    }

    public String getRuleRunId() {
        return ruleRunId;
    }

    public void setRuleRunId(String ruleRunId) {
        this.ruleRunId = ruleRunId;
    }

    public long  getResultCount() {
        if(resultCount==null) {
            return 0;
        } else {
            return resultCount;
        }
    }

    public void setResultCount(Long resultCount) {
        this.resultCount = resultCount;
    }

    public RuleRun(String ruleRunId){
        this.ruleRunId = ruleRunId;
        this.resultLineN = new StringBuffer();
        this.resultChartData = new StringBuffer();
        this.hdfsFolder = null;
    }

//    public RuleRun(String ruleId, Timestamp submitTime) {
//        this.ruleId = ruleId;
//        this.submitTime = submitTime;
//        this.resultLineN = new StringBuffer();
//        this.resultChartData = new StringBuffer();
//        this.hdfsFolder = null;
//    }

    public RuleRun(String ruleRunId, String ruleId, Timestamp submitTime, String taskProgram, String ruleProg, String chartProg){
        this.ruleRunId = ruleRunId;
        this.ruleId = ruleId;
        this.submitTime = submitTime;
        this.chartProg = chartProg;
        this.ruleProg = ruleProg;
        this.taskProgram = taskProgram;
        this.resultLineN = new StringBuffer();
        this.resultChartData = new StringBuffer();

    }

    public RuleRun(String ruleId, Timestamp submitTime, String taskProgram, String ruleProg, String chartProg) {
        this.ruleId = ruleId;
        this.submitTime = submitTime;
        this.chartProg = chartProg;
        this.ruleProg = ruleProg;
        this.taskProgram = taskProgram;
        this.resultLineN = new StringBuffer();
        this.resultChartData = new StringBuffer();
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public Timestamp getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Timestamp submitTime) {
        this.submitTime = submitTime;
    }

    public Timestamp getSubmitTimeResult() {
        return submitTimeResult;
    }

    public void setSubmitTimeResult(Timestamp submitTimeResult) {
        this.submitTimeResult = submitTimeResult;
    }

    public String getTaskProgram() {
        return taskProgram;
    }

    public void setTaskProgram(String taskProgram) {
        this.taskProgram = taskProgram;
    }

    public String getRuleProg() {
        return ruleProg;
    }

    public void setRuleProg(String ruleProg) {
        this.ruleProg = ruleProg;
    }

    public String getChartProg() {
        return chartProg;
    }

    public void setChartProg(String chartProg) {
        this.chartProg = chartProg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getRunTime() {
        return runTime;
    }

    public void setRunTime(Timestamp runTime) {
        this.runTime = runTime;
    }

    public StringBuffer getResultLineN() {
        return resultLineN;
    }

    public void setResultLineN(StringBuffer resultLineN) {
        this.resultLineN = resultLineN;
    }


    public StringBuffer getResultChartData() {
        return resultChartData;
    }

    public void setResultChartData(StringBuffer resultChartData) {
        this.resultChartData = resultChartData;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Timestamp getUpdate() {
        return update;
    }

    public String getSubmitTimeStr() {
        return simpleDateFormat.format(submitTime);
    }

    public String getRuleFileName() {
        return ruleId + "_" + sdf.format(submitTime);
    }

    public String getUpdateTimeStr() {
        return simpleDateFormat.format(update);
    }

    public void setUpdate(Timestamp update) {
        this.update = update;
    }

    public String getHdfsFolder() {
        return hdfsFolder;
    }

    public void setHdfsFolder(String hdfsFolder) {
        this.hdfsFolder = hdfsFolder;
    }

    public String getRuleCount() {
        return ruleCount;
    }

    public void setRuleCount(String ruleCount) {
        this.ruleCount = ruleCount;
    }

    public long getCheckCount() {
        return checkCount;
    }

    public void setCheckCount(int checkCount) {
        this.checkCount = checkCount;
    }

}
