package com.ylink.aml.modular.system.service;

import com.ylink.aml.modular.system.entity.TargetTabCol;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface TargetTabColService extends IService<TargetTabCol> {
    //根据tableName查询表字段定义信息
    List<TargetTabCol> selectTabColInfo(String tableName);
    //根据表名查询表字段
    List<String> selectColumnName(String tableName);
    //统计每个表对应的字段个数
    List<Map<String, Integer>>  countFieldNum();
}
