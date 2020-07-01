package com.ylink.aml.modular.system.mapper;

import com.ylink.aml.modular.system.entity.RuleRunD;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ylink.aml.modular.system.entity.RuleRunViewEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 菜单表 Mapper 接口
 * </p>
 *
 * @since 2018-12-07
 */
@Repository
public interface RuleRunMapper extends BaseMapper<RuleRunD> {
    List<RuleRunD> selectBy(String ruleId);


    void updateStart(@Param("autoCheckId")Integer autoCheckId);



}
