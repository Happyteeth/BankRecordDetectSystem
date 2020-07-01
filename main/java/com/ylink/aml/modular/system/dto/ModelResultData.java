package com.ylink.aml.modular.system.dto;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class ModelResultData {

    /**
     * 数量条数
     */
    private  long count;
    /**
     * 数据list集合
     */
    private List<Map<String,String>> data;

    /**
     * 字段map集合
     */
    private LinkedHashMap<String,String> title;

   private String ruleRunId;
    /**
     *  违规占比
     */
    private String proportion;
    /**
     *数据SQL执行结果总记录数
     */
    private long resultCount;

    /**
     * 需要高亮数据的坐标
     */
    private int[][] coordinateArr;
}
