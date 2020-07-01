package com.ylink.aml.modular.system.model;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ModelAndPararunDto implements Serializable {

    private String ruleId;
    /**
     * 	规则或模型名称
     */
    private String ruleName;
    /**
     * 		模型描述
     */
    private String modelDesc;

    /**
     * 	页面配置项
     */
    private String ruleWeb;

    /**
     * 规则检测执行语言
     */
    private String taskProgram;

    /**
     * 	实际检测的SQL或程序
     */
    private String ruleProg;

    /**
     * 图标的样式
     */
    private String chartType;

    /**
     *  图表SQL或程序
     */
    private String chartProg;

    /**
     * 规则分类
     */
    private String ruleType;
    /**
     * 模型来源
     */
    private String modelSource;

    /**
     * 模型分类第一级
     */
    private String modelType1ItemId;

    /**
     * 	模型分类第二级
     */

    private String modelType2ItemId;
    /**
     * 删除标志
     */
    private String cDelFlag;

    /**
     * 	操作时间（新增）
     */

    private Date dInsert;
    /**
     * 操作时间（最后修改）
     */
    private Date dUpdate;

    /**
     * 	操作人（新增）
     */

    private String vInsertUser;
    /**
     * 操作人（最后修改）
     */
    private String vUpdateUser;

    /**
     * 	模型分类第一级值id
     */

    private String modelType1ItemValId;
    /**
     * 模型分类第二级值id
     */
    private String modelType2ItemValId;
    private String paraString;
    /**
     * 参数名说明
     */
    private String paraDesc;
    /**
     * 	参数值
     */
    private String paraValue;



}
