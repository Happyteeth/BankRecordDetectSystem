
package com.ylink.aml.core.common.constant;

import cn.hutool.core.collection.CollectionUtil;

import java.util.List;

/**
 * 系统常量
 *
 * @author lida
 * @Date 2019年2月12日 下午9:42:53
 */
public interface Const {

    /**
     * 默认管理系统的名称
     */
    String DEFAULT_SYSTEM_NAME = "反洗钱智能监测平台";


    /**
     * 默认管理系统的名称
     */
    String BANK_SYSTEM_NAME = "反洗钱智能监测平台";

    /**
     * 默认管理系统的名称
     */
    String PAY_SYSTEM_NAME = "反洗钱智能监测平台";
    /**
     * 默认欢迎界面的提示
     */
    String DEFAULT_WELCOME_TIP = "欢迎使用反洗钱智能监测平台!";

    /**
     * 应用版本  大数据版 bigData
     */
    String  APP_VERSION_BIGDATA ="bigData";

    /**
     * 应用版本  单机版本 home
     */
    String  APP_VERSION_HOME ="home";

    /**
     * 银行版 bank
     */
    String  APP_TYPE_BANK ="bank";

    /**
     * 支付版 pay
     */
    String  APP_TYPE_PAY ="pay";

    /**
     * 系统默认的管理员密码
     */
    String DEFAULT_PWD = "111111";

    /**
     * 管理员角色的名字
     */
    String ADMIN_NAME = "administrator";

    /**
     * 管理员id
     */
    Long ADMIN_ID = 1L;

    /**
     * 超级管理员角色id
     */
    Long ADMIN_ROLE_ID = 1L;

    /**
     * 接口文档的菜单名
     */
    String API_MENU_NAME = "接口文档";

    /**
     * 不需要权限验证的资源表达式
     */
    List<String> NONE_PERMISSION_RES = CollectionUtil.newLinkedList("/assets/**", "/api/**", "/login", "/global/sessionError", "/kaptcha", "/error", "/global/error");

}
