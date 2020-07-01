package com.ylink.aml.modular.system.service.impl;

import com.ylink.aml.modular.system.entity.TbsProgPara;
import com.ylink.aml.modular.system.mapper.TbsProgParaMapper;
import com.ylink.aml.modular.system.service.TbsProgParaService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/6/28 12:45
 */
@AllArgsConstructor
@Service
public class TbsProgParaServiceImpl extends ServiceImpl<TbsProgParaMapper, TbsProgPara> implements TbsProgParaService {
    private TbsProgParaMapper mapper;
    @Override
    public String selectParaValue(String vProgId, String vParaId) {
        TbsProgPara tbsProgPara = mapper.selectOne(new QueryWrapper<TbsProgPara>()
                .eq("V_PROG_ID", vProgId)
                .eq("V_PARA_ID", vParaId));
        String paraValue = tbsProgPara.getVParaValue();
        return paraValue;
    }
}
