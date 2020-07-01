
package com.ylink.aml.core.beetl;

import com.ylink.aml.config.properties.GunsProperties;
import com.ylink.aml.core.common.constant.Const;
import com.ylink.aml.core.util.KaptchaUtil;
import cn.stylefeng.roses.core.util.ToolUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.beetl.ext.spring.BeetlGroupUtilConfiguration;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

import static com.ylink.aml.core.common.constant.Const.*;

/**
 * beetl拓展配置,绑定一些工具类,方便在模板中直接调用
 *
 * @author lida
 * @Date 2018/2/22 21:03
 */
@Slf4j
public class BeetlConfiguration extends BeetlGroupUtilConfiguration {

    @Value("${aml.appType}")
    private String appType;

    @Override
    public void initOther() {

        //全局共享变量
        Map<String, Object> shared = new HashMap<>();
        log.info(">>>>>>>>>>"+Const.APP_TYPE_BANK);
        log.info("><<<<<<<<<<<"+appType);
        if(Const.APP_TYPE_BANK.equalsIgnoreCase(appType)) {
            shared.put("systemName", BANK_SYSTEM_NAME);
        }else if(Const.APP_TYPE_PAY.equalsIgnoreCase(appType)){
            shared.put("systemName", PAY_SYSTEM_NAME);
        }else {
            shared.put("systemName", DEFAULT_SYSTEM_NAME);
        }
        shared.put("welcomeTip", DEFAULT_WELCOME_TIP);
        groupTemplate.setSharedVars(shared);

        //全局共享方法
        groupTemplate.registerFunctionPackage("shiro", new ShiroExt());
        groupTemplate.registerFunctionPackage("tool", new ToolUtil());
        groupTemplate.registerFunctionPackage("kaptcha", new KaptchaUtil());
    }

}
