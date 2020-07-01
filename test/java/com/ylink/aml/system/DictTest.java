package com.ylink.aml.system;

import com.ylink.aml.base.BaseJunit;
import com.ylink.aml.modular.system.mapper.DictMapper;
import com.ylink.aml.modular.system.service.DictService;
import org.junit.Test;

import javax.annotation.Resource;

/**
 * 字典服务测试
 *
 * @author lida
 * @Date 2019-04-27 17:05
 */
public class DictTest extends BaseJunit {

    @Resource
    DictService dictService;

    @Resource
    DictMapper dictMapper;

    @Test
    public void deleteTest() {
        this.dictService.delteDict(16L);
    }
}
