package com.ylink.aml.modular.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ylink.aml.modular.system.entity.RuleAutoCheckView;

/**
 * <p>
 * 一键检测批次定义 Mapper 接口
 * </p>
 *
 * @author lida
 * @since 2019-07-10
 */
public interface RuleAutoCheckViewMapper extends BaseMapper<RuleAutoCheckView> {
    Integer getMaxRuleAutoCheckId();

    RuleAutoCheckView getAutoCheckMax();
}
