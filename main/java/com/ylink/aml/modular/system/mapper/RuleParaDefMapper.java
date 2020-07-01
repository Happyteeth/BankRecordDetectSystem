package com.ylink.aml.modular.system.mapper;


import com.ylink.aml.modular.system.entity.RuleParaDef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ylink.aml.modular.system.entity.Rulerunpara;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleParaDefMapper extends BaseMapper<RuleParaDef> {


    List<RuleParaDef> findByexcel();



    List<RuleParaDef> selectByIdl(@Param("ruleId") String ruleId);

}
