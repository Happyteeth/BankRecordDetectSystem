package com.ylink.aml.modular.system.service;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cfss.rules.RuleStaticInfo;
import com.ylink.aml.core.shiro.ShiroKit;
import com.ylink.aml.modular.system.entity.Model;
import com.ylink.aml.modular.system.entity.RuleParaDef;
import com.ylink.aml.modular.system.entity.RuleRunD;
import com.ylink.aml.modular.system.mapper.RuleParaDefMapper;
import com.ylink.aml.modular.system.mapper.RuleRunMapper;
import com.ylink.aml.modular.system.util.YlCollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;


/**
 * </p>
 *
 * @since 2019-06-07
 */


@Service
@Slf4j
public class RuleRunService extends ServiceImpl<RuleRunMapper, RuleRunD> {
    @Autowired
    private RuleParaDefMapper ruleParaDefMapper;

    public List<RuleRunD> selectBy(String ruleId) {
        return baseMapper.selectBy(ruleId);
    }

    public void insert(RuleRunD ruleRun) {
        baseMapper.insert(ruleRun);
    }

    public boolean addRuleRun(List<Model> modelList) {

        List<RuleRunD> ruleRunDList = YlCollUtil.copyList(modelList, RuleRunD.class);
        for (RuleRunD ruleRun : ruleRunDList) {

            List<RuleParaDef> ruleParaDefs = ruleParaDefMapper.selectByIdl(ruleRun.getRuleId());
            for (RuleParaDef ruleParaDef : ruleParaDefs) {

            }
            ruleRun.setStatus(String.valueOf(RuleStaticInfo.RULE_STATUS_SUBMIT));
            ruleRun.setDInsert(new Timestamp(System.currentTimeMillis()));
            ruleRun.setVInsertUser(ShiroKit.getUser().getName());
            int num = this.baseMapper.insert(ruleRun);
            if (num <= 0) {
                return false;
            }
        }
        return true;
    }





    public void updateStart(Integer autoCheckId) {
        baseMapper.updateStart(autoCheckId);
    }
}
