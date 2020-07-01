package com.ylink.aml.modular.system.mapper;

import com.ylink.aml.modular.system.entity.Model;
import com.ylink.aml.modular.system.model.*;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ModelMapper extends BaseMapper<Model> {











    List<Model> findexcellist();











    List<DictionaryValAndmodelDto> selectruleName(@Param("ruleName") String ruleName);


    void updateModel(@Param("modelType1ItemValId") String modelType1ItemValId,
                     @Param("modelType2ItemValId") String modelType2ItemValId,
                     @Param("ruleId")  String ruleId,
                     @Param("ruleName")  String ruleName);





    ModelDto selectModelV(@Param("ruleId") String ruleId);



    List<ModelDto> selectModelt(@Param("ruleId") String ruleId);

    List<ModelvalDto> selectval();
}