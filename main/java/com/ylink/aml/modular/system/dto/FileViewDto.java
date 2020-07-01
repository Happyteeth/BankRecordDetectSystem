package com.ylink.aml.modular.system.dto;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/7/18 16:28
 */
@Data
public class FileViewDto {
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
}
