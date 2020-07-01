package com.ylink.aml.modular.system.controller;

import cn.stylefeng.roses.core.base.controller.BaseController;
import com.ylink.aml.core.common.constant.Const;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/9/23 11:29
 */
@Slf4j
@Controller
@AllArgsConstructor
@RequestMapping("/frame")
public class VersionController extends BaseController {
    private static String PREFIX = "/modular/frame/";
    /*
     * @Description：跳到版本管理页面
     * @param: []
     * @return java.lang.String
     */
    @RequestMapping("/version_info")
    public String index() {
            return PREFIX + "version_info.html";
    }
}
