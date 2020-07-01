package com.ylink.aml.modular.system.model;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ModelDto implements Serializable {

    private String ruleId;
    /**
     * 	模型分类第一级值id
     */

    private String modelType1ItemValId;
    /**
     * 模型分类第二级值id
     */
    private String modelType2ItemValId;
    /**
     * 	规则说明
     */
    private String modelDesc;
    /**
     * 	实际检测的SQL或程序
     */
    private String ruleProg;
    /**
     *  图表SQL或程序
     */
    private String chartProg;
    /**
     * 模型名称
     */
    private String ruleName;
    /**
     * 模型分类第一级
     */
    private String modelType1ItemId;

    /**
     * 	模型分类第二级
     */

    private String modelType2ItemId;
    /**
     * 	操作时间（新增）
     */

    private Date dInsert;
    /**
     * 	参数名字符串
     */
    private String paraString;
    /**
     * 	参数值
     */
    private String paraValue;
    /**
     * 参数名说明
     */
    private String paraDesc;
    /**
     * 模型代码
     */
    private String ModelSql;
    /**
     * 	操作人（新增）
     */

    private String vInsertUser;
    /**
     * 字典表对应第一级id
     */
    private String vItemValId1;
    /**
     * 	字典表对应第二级id
     */

    private String vItemValId2;

    /**
     *是否参与一键检测：0 - 不参与；1 - 参与
     */

    private String ifAutoCheck;

}
