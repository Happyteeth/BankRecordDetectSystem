package com.ylink.aml.modular.system.mapper;


import com.ylink.aml.modular.system.entity.TbsDictionaryVal;
import com.ylink.aml.modular.system.model.DictValDto;
import com.ylink.aml.modular.system.model.OrdDto;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yzd
 * @since 2019-04-29
 */
@Repository
public interface TbsDictionaryValMapper extends BaseMapper<TbsDictionaryVal> {



    List<TbsDictionaryVal> selecttbsDictionaryVal(@Param("vItemId") String vItemId);

    List<TbsDictionaryVal> selectId(@Param("vItemValId") String vItemValId);

    void updateId(@Param("vItemValId") String cvalid,@Param("vItemId") String cid);

    List<TbsDictionaryVal> selecttbsVal(@Param("vItemValIdPar") String vItemValIdPar);

    DictValDto selectvval(@Param("vItemId") String vItemId);

    OrdDto selectorde();


    // TbsDictionaryVal selectmodelId1( @Param("valId1")  String valId1,@Param("modelId1")  String modelId1);

   /* List<TbsDictionaryVal> selecttbsDictionary(@Param("vValName") String vValName);

    List<TbsDictionaryVal> slectvTtemval(@Param("vItemValIdPar") String vItemValIdPar);

    List<TbsDictionaryVal> selectByvItenId(@Param("vItenId") String vItenId);*/
}
