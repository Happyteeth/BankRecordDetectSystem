package com.ylink.aml.modular.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author yzd
 * @since 2019-04-29
 */
@Data
@TableName("V_RULE_RUN")
public class RuleRunViewEntity implements Serializable {

    private static final long serialVersionUID = -8489843619109193919L;

    //执行id
    @TableId(value = "rule_run_id", type = IdType.INPUT)
    private String ruleRunId;

    private String ruleId;

    //提交执行时间
    private Timestamp submitTime;
    //规则检测执行语言： 0 -- SQL， 1 -- Oracle 2 -- Python
    private String taskProgram;
    // 数据SQL或程序
    private String ruleProg;
    //图表SQL或程序
    private String chartProg;
    //状态 1 -- 初始提交；2 -- 运行中 ；8 -- 执行完成；9 -- 执行异常
    private String status;
    //执行完成时间
    private Date runTime;
    //数据SQL执行结果的前N行，使用标准格式
    private String rerultLineN;
    //执行结果文件路径：包含目录和文件名 文件名：RULE_ID+SUBMIT_TIME
    private String rerultPath;
    //执行结果图表数据（标准格式）
    private String rerultChartData;

    private String appId;
    private String rerultChart;
    private Date dInsert;
    private Date dUpdate;
    private String vInsertUser;
    private long rerultCount;
    private int autoCheckId;
    private long rulePriority;
}
