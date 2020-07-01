package com.ylink.aml.modular.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ylink.aml.modular.system.entity.RuleAutoCheckView;
import com.ylink.aml.modular.system.entity.RuleRunViewEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface RuleAutoCheckViewService extends IService<RuleAutoCheckView> {
    Integer getMaxRuleAutoCheckId();
    RuleAutoCheckView getAutoCheckInfo(Integer autoCheckId);
    RuleAutoCheckView getAutoCheckMax();
}
