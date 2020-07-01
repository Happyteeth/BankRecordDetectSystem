package com.ylink.aml.modular.system.service;

import com.ylink.aml.modular.system.entity.TbsProgPara;
import com.baomidou.mybatisplus.extension.service.IService;

public interface TbsProgParaService extends IService<TbsProgPara> {
    //根据程序id和参数id获取参数值
    String selectParaValue(String vProgId, String vParaId);
}
