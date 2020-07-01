package com.ylink.aml.modular.system.service;


import com.ylink.aml.modular.system.entity.RuleParaDef;
import com.ylink.aml.modular.system.entity.Rulerunpara;
import com.ylink.aml.modular.system.mapper.RuleParaDefMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RuleParaDefService extends ServiceImpl<RuleParaDefMapper, RuleParaDef> {








    public List<RuleParaDef> selectByIdl(String ruleId) {
        return baseMapper.selectByIdl(ruleId);
    }


}