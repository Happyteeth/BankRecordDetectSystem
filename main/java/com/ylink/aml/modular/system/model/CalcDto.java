package com.ylink.aml.modular.system.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author qy
 */
@Data
public class CalcDto implements Serializable {

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
     * 依照什么统计 -- 中文
     */
    private String chGroupBy;
}
