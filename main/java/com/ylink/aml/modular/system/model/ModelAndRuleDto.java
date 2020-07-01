package com.ylink.aml.modular.system.model;


import lombok.Data;

import java.io.Serializable;

@Data
public class ModelAndRuleDto implements Serializable {

    private String ruleId;

    /**
     * 	规则说明
     */
    private String modelDesc;

    /**
     * 模型名称
     */
    private String ruleName;

    /**
     * 	参数值
     */
    private String paraValue;

}
