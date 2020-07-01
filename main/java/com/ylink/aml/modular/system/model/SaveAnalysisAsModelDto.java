package com.ylink.aml.modular.system.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author qy
 */
@Data
public class SaveAnalysisAsModelDto implements Serializable {

    private static final long serialVersionUID = 1823411071265350938L;
    /**
     * 统计的表名
     */
    private String tableName;

    private List<String> columnList;

    private List<String> chColumnList;

    private List<ConditionDto> conditionList;

    private String relation;

    /**
     * 依照什么统计
     */
    private String groupBy;


    /**
     * 统计方式及统计的列
     * eg : key - SUM(中文列名)； value - 英文列名
     */
    private Map<String, String>[] calcMap;


    /**
     * 模型名称
     */
    private String ruleName;

    /**
     * 图表样式
     */
    private String chartType;

    private String modelDesc;

    private String modelType1ItemId;

    private String modelType1ItemValId;

    private String modelType2ItemId;

    private String modelType2ItemValId;

    private String chartVal;

    private String chartX;

}
