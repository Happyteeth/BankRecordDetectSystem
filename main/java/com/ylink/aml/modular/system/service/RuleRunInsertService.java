package com.ylink.aml.modular.system.service;

import cn.stylefeng.roses.core.reqres.response.ResponseData;
import com.ylink.aml.modular.system.entity.Model;
import com.ylink.aml.modular.system.entity.RuleDef;

public interface RuleRunInsertService {

    /**
     * 生成执行实例
     * @param model
     * @param autoCheckId
     * @return
     */
    String  createRuleRun(Model model, int autoCheckId);

    String createRuleRunPay(RuleDef ruleDef, int autoCheckId);
}
