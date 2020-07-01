package com.ylink.aml.modular.system.service;



import com.ylink.aml.modular.system.entity.TbsDictionaryVal;
import com.ylink.aml.modular.system.mapper.ModelMapper;
import com.ylink.aml.modular.system.mapper.TbsDictionaryValMapper;
import com.ylink.aml.modular.system.model.DictValDto;
import com.ylink.aml.modular.system.model.OrdDto;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TbsDictionaryValService extends ServiceImpl<TbsDictionaryValMapper, TbsDictionaryVal> {


    @Resource
    private ModelMapper modelMapper;




   /* public List<DictionaryValAndmodelDto> selecttbsDictionaryVal(String vItemId) {
        List<DictionaryValAndmodelDto> list= baseMapper.selecttbsDictionaryVal(vItemId);
        for (DictionaryValAndmodelDto dictionaryValAndmodelDto: list) {
            List<DictionaryValAndmodelDto> mod=modelMapper.selectBylist(dictionaryValAndmodelDto.getVItemValIdPar(),dictionaryValAndmodelDto.getVItemId(),dictionaryValAndmodelDto.getVItemValId());
            for(DictionaryValAndmodelDto valAndmodelDto :mod){
                valAndmodelDto.setVItemValIdPar(valAndmodelDto.getModelType1ItemValId());
                valAndmodelDto.setVItemValId(valAndmodelDto.getModelType2ItemValId());
                list.add(valAndmodelDto);
            }
        }

        return selectree(list);
    }*/







    public List<TbsDictionaryVal> selecttbsDictionaryVal(String vItemId) {
        List<TbsDictionaryVal> tbsDictionaryVal = baseMapper.selecttbsDictionaryVal(vItemId);
        return tbsDictionaryVal;
    }

    public List<TbsDictionaryVal> selectId(String vItemValId) {
        List<TbsDictionaryVal> tbsDictionaryVal = baseMapper.selectId(vItemValId);
        return tbsDictionaryVal;
    }





    public void updateId(String cvalid, String cid) {
        baseMapper.updateId(cvalid,cid);
    }

    public int insert(TbsDictionaryVal dictionaryVal) {
        return  baseMapper.insert(dictionaryVal);
    }

    public List<TbsDictionaryVal> selecttbsVal(String vItemValIdPar) {
        return baseMapper.selecttbsVal(vItemValIdPar);
    }



    public OrdDto selectorde() {
        return baseMapper.selectorde();
    }

    public DictValDto selectvval(String vItemId) {
        System.out.println(vItemId+"、、、、、、、、、、、、、、、、、、、、、、、、、、？");
        return baseMapper.selectvval(vItemId);
    }










   /* public List<TbsDictionaryVal> selecttbsDictionary(String vValName) {
        return tbsDictionaryValMapper.selecttbsDictionary(vValName);
    }

    public List<TbsDictionaryVal> selectByvItenId(String vItenId) {
        return  tbsDictionaryValMapper.selectByvItenId(vItenId);
    }

    public List<TbsDictionaryVal> slectvTtemval(String vItemValIdPar) {
        return tbsDictionaryValMapper.slectvTtemval(vItemValIdPar);
    }*/
}
