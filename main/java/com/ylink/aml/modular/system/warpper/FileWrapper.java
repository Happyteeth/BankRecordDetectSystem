package com.ylink.aml.modular.system.warpper;

import cn.stylefeng.roses.core.base.warpper.BaseControllerWrapper;
import cn.stylefeng.roses.core.util.ToolUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;

/**
 * @Description:数据导入信息列表包装类
 * @Auther: lida
 * @Date: 2019/6/29 17:39
 */
public class FileWrapper extends BaseControllerWrapper {
    public FileWrapper(Page<Map<String, Object>> page) {
        super(page);
    }

    public FileWrapper(Map<String, Object> single) {
        super(single);
    }

    @Override
    protected void wrapTheMap(Map<String, Object> map) {
        String lineN = (String) map.get("lineN");

        //如果信息中包含分割符号"\r\n"   则分割字符串返给前台
        if (ToolUtil.isNotEmpty(lineN) && lineN.contains("\r\n")) {
            String[] msgs = lineN.split("\r\n",-1);
            map.put("lineN", msgs);
        } else {
            map.put("lineN", lineN);
        }
    }
}
