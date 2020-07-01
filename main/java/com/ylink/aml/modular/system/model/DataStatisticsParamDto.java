package com.ylink.aml.modular.system.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author qy
 */
@Data
public class DataStatisticsParamDto implements Serializable {

    /**
     * 统计的表名
     */
    private String tableName;

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
     * 统计条件
     */
    private List<ConditionDto> conditionList;

    /**
     * 统计条件关联关系 and/or
     */
    private String relation;


    /**
     * eCharts图表base64编码
     */
    private String chartCode;

    /**
     * 依照什么统计 -- 中文
     */
    private String chGroupBy;
}
