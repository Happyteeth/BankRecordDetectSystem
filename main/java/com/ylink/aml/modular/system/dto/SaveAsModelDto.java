package com.ylink.aml.modular.system.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author qpjun
 */
@Data
public class SaveAsModelDto implements Serializable {

    /**
     * 模型执行ID
     */
    private String ruleRunId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型描述
     */
    private String modelDesc;

    /**
     * 一级分类项ID
     */
    private String modelType1ItemId;

    /**
     * 一级分类值ID
     */
    private String modelType1ItemValId;

    /**
     * 二级分类项ID
     */
    private String modelType2ItemId;

    /**
     * 二级分类值ID
     */
    private String modelType2ItemValId;
}
