package com.ylink.aml.modular.system.mapper;

import com.ylink.aml.modular.system.entity.TargetTabCol;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TargetTabColMapper extends BaseMapper<TargetTabCol> {
    List<String> selectColumnName(String tableName);
    List<Map<String ,Integer>> countFieldNum();
}
