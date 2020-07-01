package com.cfss.util.tasklog;

public class CLevel {
    //         C_LEVEL              char(1) not null comment '日志的等级：0－正常；1－警告；2－遇到错误，程序继续运行；3－遇到错误，程序中止运行；8－勾核；9－系统功能操作日志',
    public final static String INFO  = "0";
    public final static String WARN = "1";
    public final static String ERROR_STILL_RUN = "2";
    public final static String ERROR_STOP = "3";
    public final static String SYS_OPT = "9";

}
