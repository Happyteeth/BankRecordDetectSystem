package com.ylink.aml.modular.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ylink.aml.modular.system.entity.RuleRunViewEntityPay;
import com.ylink.aml.modular.system.mapper.RuleRunViewPayMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 *
 * </p>
 *
 * @since 2019-06-07
 */
@Service
@Slf4j
public class RuleRunViewPayService extends ServiceImpl<RuleRunViewPayMapper, RuleRunViewEntityPay> {
    @Autowired
    private ModelService modelService;

    public List<RuleRunViewEntityPay> getRuleRunList(Integer autoCheckId) {
        return this.baseMapper.selectList(new QueryWrapper<RuleRunViewEntityPay>()
                .eq("AUTO_CHECK_ID", autoCheckId).orderByAsc("SUBMIT_TIME"));
    }

    public RuleRunViewEntityPay selectById(String ruleRunId) {
        return baseMapper.selectById(ruleRunId);
    }
}