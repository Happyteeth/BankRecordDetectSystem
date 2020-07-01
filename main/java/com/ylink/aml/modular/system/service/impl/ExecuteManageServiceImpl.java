package com.ylink.aml.modular.system.service.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ylink.aml.core.common.page.LayuiPageFactory;
import com.ylink.aml.core.shiro.ShiroKit;
import com.ylink.aml.core.shiro.ShiroUser;
import com.ylink.aml.modular.system.dto.SaveAsModelDto;
import com.ylink.aml.modular.system.entity.Model;
import com.ylink.aml.modular.system.mapper.ExecuteManageMapper;
import com.ylink.aml.modular.system.mapper.ModelMapper;
import com.ylink.aml.modular.system.service.IExecuteManageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author qy
 */
@Service
@Slf4j
@AllArgsConstructor
public class ExecuteManageServiceImpl implements IExecuteManageService {

    private ExecuteManageMapper executeManageMapper;

    private ModelMapper modelMapper;

    private static final String ANALYSIS_RULE_TYPE = "2";

    @Override
    public Page<Map<String, Object>> list(boolean isAll, String ruleType, String fuzzy, String sDate, String eDate, String field, String order) {
        Page page = LayuiPageFactory.defaultPage();
        Map<String, Object> params = MapUtil.newHashMap();
        String account = null;
        if (!isAll) {
            ShiroUser shiroUser = ShiroKit.getUser();
            if (shiroUser != null) {
                account = shiroUser.getAccount();
            }
            params.put("vInsertUser", account);
        }
        if (StringUtils.isNotEmpty(ruleType)) {
            params.put("ruleType", ruleType);
        }
        if (StringUtils.isNotEmpty(fuzzy)) {
            params.put("fuzzy", "%" + fuzzy + "%");
        }
        if (StringUtils.isNotEmpty(sDate)) {
            params.put("sDate", sDate);
        }
        if (StringUtils.isNotEmpty(eDate)) {
            params.put("eDate", eDate);
        }
        if (StringUtils.isEmpty(field) || StringUtils.isEmpty(order)) {
            field = "t2.SUBMIT_TIME";
            order = "DESC";
        }
        params.put("orderBy", field + " " + order);
        return executeManageMapper.list(page, params);
    }

    @Override
    public boolean saveAsModel(String ruleId, SaveAsModelDto saveAsModelDto) {
        if (StringUtils.isEmpty(ruleId)) {
            log.error("模型ID为空");
            return false;
        }
        Model model = modelMapper.selectById(ruleId);
        if (model == null) {
            log.error("分析数据不存在");
            return false;
        }
        if (ANALYSIS_RULE_TYPE.equals(model.getRuleType())) {
            log.info("模型直接返回");
            return true;
        }
        model.setRuleType(ANALYSIS_RULE_TYPE);
        //模型名称
        if (StringUtils.isNotEmpty(saveAsModelDto.getModelName())) {
            model.setRuleName(saveAsModelDto.getModelName());
        }
        //模型描述
        if (StringUtils.isNotEmpty(saveAsModelDto.getModelDesc())) {
            model.setModelDesc(saveAsModelDto.getModelDesc());
        }
        //一二级分类信息
        model.setModelType1ItemId(saveAsModelDto.getModelType1ItemId());
        model.setModelType1ItemValId(saveAsModelDto.getModelType1ItemValId());
        model.setModelType2ItemId(saveAsModelDto.getModelType2ItemId());
        model.setModelType2ItemValId(saveAsModelDto.getModelType2ItemValId());
        //设置不参与一键检测
        model.setIfAutoCheck("0");
        int result = modelMapper.updateById(model);
        if (result > 0) {
            return true;
        }
        log.error("保存模型失败");
        return false;
    }
}
