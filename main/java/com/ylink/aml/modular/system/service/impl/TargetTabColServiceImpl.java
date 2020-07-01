package com.ylink.aml.modular.system.service.impl;

import com.ylink.aml.modular.system.entity.TargetTabCol;
import com.ylink.aml.modular.system.mapper.TargetTabColMapper;
import com.ylink.aml.modular.system.service.TargetTabColService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/6/25 14:45
 */
@AllArgsConstructor
@Service
public class TargetTabColServiceImpl extends ServiceImpl<TargetTabColMapper, TargetTabCol> implements TargetTabColService {

    private TargetTabColMapper targetTabColMapper;

    @Override
    public List<TargetTabCol> selectTabColInfo(String tableName) {
        return targetTabColMapper.selectList(new QueryWrapper<TargetTabCol>()
                .eq("TABLE_NAME", tableName).orderByAsc("COLUMN_SEQ"));
    }

    @Override
    public List<String> selectColumnName(String tableName) {
        return targetTabColMapper.selectColumnName(tableName);
    }

    @Override
    public List<Map<String, Integer>> countFieldNum() {
        return  targetTabColMapper.countFieldNum();

    }
}
