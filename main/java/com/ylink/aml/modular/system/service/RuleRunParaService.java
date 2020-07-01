package com.ylink.aml.modular.system.service;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ylink.aml.modular.system.entity.Rulerunpara;
import com.ylink.aml.modular.system.mapper.RuleRunParaMapper;
import org.springframework.stereotype.Service;

/**
 *
 *
 * </p>
 *
 * @since 2019-06-07
 */
@Service
public class RuleRunParaService extends ServiceImpl<RuleRunParaMapper, Rulerunpara> {


    public void insert(Rulerunpara rulerunpara) {

        baseMapper.insert(rulerunpara);
    }
}
