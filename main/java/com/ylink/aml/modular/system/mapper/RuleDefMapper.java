package com.ylink.aml.modular.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ylink.aml.modular.system.entity.RuleDef;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface RuleDefMapper extends BaseMapper<RuleDef> {
    List<RuleDef> findexcellist();
}