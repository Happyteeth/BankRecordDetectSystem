
package com.ylink.aml.modular.system.controller;

import cn.hutool.core.bean.BeanUtil;
import com.ylink.aml.core.common.annotion.BussinessLog;
import com.ylink.aml.core.common.annotion.Permission;
import com.ylink.aml.core.common.constant.Const;
import com.ylink.aml.core.common.constant.state.BizLogType;
import com.ylink.aml.core.common.page.LayuiPageFactory;
import com.ylink.aml.modular.system.entity.OperationLog;
import com.ylink.aml.modular.system.service.OperationLogService;
import com.ylink.aml.modular.system.warpper.LogWrapper;
import cn.stylefeng.roses.core.base.controller.BaseController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 日志管理的控制器
 *
 * @author lida
 * @Date 2019年4月5日 19:45:36
 */
@Controller
@RequestMapping("/log")
public class LogController extends BaseController {

    private static String PREFIX = "/modular/system/log/";

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 跳转到日志管理的首页
     *
     * @author lida
     * @Date 2018/12/23 5:34 PM
     */
    @RequestMapping("")
    public String index() {
        return PREFIX + "log.html";
    }

    /**
     * 查询操作日志列表
     *
     * @author lida
     * @Date 2018/12/23 5:34 PM
     */
    @RequestMapping("/list")
    @Permission(Const.ADMIN_NAME)
    @ResponseBody
    public Object list(@RequestParam(required = false) String beginTime,
                       @RequestParam(required = false) String endTime,
                       @RequestParam(required = false) String logName,
                       @RequestParam(required = false) Integer logType) {

        //获取分页参数
        Page page = LayuiPageFactory.defaultPage();

        //根据条件查询操作日志
        List<Map<String, Object>> result = operationLogService.getOperationLogs(page, beginTime, endTime, logName, BizLogType.valueOf(logType));

        page.setRecords(new LogWrapper(result).wrap());

        return LayuiPageFactory.createPageInfo(page);
    }

    /**
     * 查询操作日志详情
     *
     * @author lida
     * @Date 2018/12/23 5:34 PM
     */
    @RequestMapping("/detail/{id}")
    @Permission(Const.ADMIN_NAME)
    @ResponseBody
    public Object detail(@PathVariable Long id) {
        OperationLog operationLog = operationLogService.getById(id);
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(operationLog);
        return super.warpObject(new LogWrapper(stringObjectMap));
    }

    /**
     * 清空日志
     *
     * @author lida
     * @Date 2018/12/23 5:34 PM
     */
    @BussinessLog(value = "清空业务日志")
    @RequestMapping("/delLog")
    @Permission(Const.ADMIN_NAME)
    @ResponseBody
    public Object delLog() {
        SqlRunner.db().delete("delete from sys_operation_log");
        return SUCCESS_TIP;
    }
}
