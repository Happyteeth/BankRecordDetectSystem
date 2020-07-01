package com.ylink.aml.modular.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ylink.aml.modular.system.dto.ModelResultData;
import com.ylink.aml.modular.system.entity.Model;
import com.ylink.aml.modular.system.entity.RuleAutoCheckView;
import com.ylink.aml.modular.system.entity.RuleRunViewEntity;
import com.ylink.aml.modular.system.mapper.RuleAutoCheckViewMapper;
import com.ylink.aml.modular.system.mapper.RuleRunMapper;
import com.ylink.aml.modular.system.service.RuleAutoCheckViewService;
import com.ylink.aml.modular.system.service.RuleRunService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/7/10 15:57
 */

@Service
public class RuleAutoCheckViewServiceImpl extends ServiceImpl<RuleAutoCheckViewMapper, RuleAutoCheckView> implements RuleAutoCheckViewService {

    @Override
    public Integer getMaxRuleAutoCheckId() {
        return this.baseMapper.getMaxRuleAutoCheckId();
    }

    @Override
    public RuleAutoCheckView getAutoCheckInfo(Integer autoCheckId) {
        RuleAutoCheckView ruleAutoCheckView = this.baseMapper.selectOne(new QueryWrapper<RuleAutoCheckView>()
        .eq("AUTO_CHECK_ID", autoCheckId));
        return ruleAutoCheckView;
    }

    @Override
    public RuleAutoCheckView getAutoCheckMax() {
        return baseMapper.getAutoCheckMax();
    }
}
