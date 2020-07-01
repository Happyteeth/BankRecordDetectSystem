package com.ylink.aml.modular.system.controller;

import cn.stylefeng.roses.core.reqres.response.ResponseData;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ylink.aml.core.common.page.LayuiPageFactory;
import com.ylink.aml.core.shiro.ShiroKit;
import com.ylink.aml.core.shiro.ShiroUser;
import com.ylink.aml.modular.system.dto.SaveAsModelDto;
import com.ylink.aml.modular.system.entity.RuleRunD;
import com.ylink.aml.modular.system.service.IExecuteManageService;
import com.ylink.aml.modular.system.service.RuleRunService;
import com.ylink.aml.modular.system.warpper.ExecuteWrapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.Map;

/**
 * @author qy
 */
@Controller
@RequestMapping(value = "/manage")
@AllArgsConstructor
public class ExecuteManageController {

    private static final String PREFIX = "/modular/executeManage/";

    private static final String[] REVISABLE_STATUS = new String[]{"1", "B"};

    private static final String ANALYSIS_COMPLETE_STATUS = "8";

    private IExecuteManageService iExecuteManageService;

    private RuleRunService ruleRunService;

    @RequestMapping("")
    public String index() {
        return PREFIX + "executeManage.html";
    }

    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(@RequestParam(required = false, value = "isAll", defaultValue = "false") boolean isAll, //true 查看全部， false 查看自己 默认为false
                       @RequestParam(required = false, value = "ruleType") String ruleType, //1 为数据分析，2 为模型执行 为空查询全部
                       @RequestParam(required = false, value = "fuzzy") String fuzzy, //模糊匹配字段 使用模型名称和模型描述进行匹配
                       @RequestParam(required = false, value = "sDate") String sDate, //提交时间 -开始时间
                       @RequestParam(required = false, value = "eDate") String eDate,
                       @RequestParam(required = false, value = "field") String field,
                       @RequestParam(required = false, value = "order") String order) { //提交时间 -结束时间
        ShiroUser user = ShiroKit.getUser();
        if (user == null) {
            return LayuiPageFactory.error("获取用户信息失败");
        }
        if (user.getRoleList() == null || user.getRoleList().size() < 1) {
            return LayuiPageFactory.error("获取用户角色失败");
        }
        if (ShiroKit.isAdmin()) {
            isAll = true;
        }
        Page<Map<String, Object>> rePage = iExecuteManageService.list(isAll, ruleType, fuzzy, sDate, eDate, field, order);
        Page<Map<String, Object>> page = new ExecuteWrapper(rePage).wrap();
        return LayuiPageFactory.createPageInfo(page);
    }

    @RequestMapping(value = "/cancel")
    @ResponseBody
    public Object cancel(@RequestParam(required = false, value = "ruleRunId") String ruleRunId, //执行ID
                         @RequestParam(required = false, value = "status") String status) { //状态
        if (StringUtils.isEmpty(ruleRunId) || StringUtils.isEmpty(status)) {
            return ResponseData.error("参数不能为空");
        }
        ShiroUser user = ShiroKit.getUser();
        if (user == null || StringUtils.isEmpty(user.getAccount())) {
            return ResponseData.error("获取登陆用户超时");
        }
        String account = user.getAccount();
        RuleRunD ruleRun = ruleRunService.getById(ruleRunId);
        if (ruleRun == null) {
            return ResponseData.error("没有查询待执行的信息");
        }
        if (status.equals(ruleRun.getStatus())) {
            return ResponseData.error("无需操作");
        }
        if (!Arrays.asList(REVISABLE_STATUS).contains(ruleRun.getStatus())) {
            return ResponseData.error("无法进行操作");
        }
        if (!account.equals(ruleRun.getVInsertUser())) {
            return ResponseData.error("只能更改自己提交");
        }
        ruleRun.setStatus(status);
        boolean res = ruleRunService.updateById(ruleRun);
        if (res) {
            return ResponseData.success();
        }
        return ResponseData.error("撤销失败");
    }

    @RequestMapping(value = "/saveAsModel")
    @ResponseBody
    public Object saveAsModel(@RequestBody SaveAsModelDto saveAsModelDto) {
        if (saveAsModelDto == null) {
            return ResponseData.error("参数不能为空");
        }
        String ruleRunId = saveAsModelDto.getRuleRunId();
        if (StringUtils.isEmpty(ruleRunId)) {
            return ResponseData.error("参数不能为空");
        }
        RuleRunD ruleRun = ruleRunService.getById(ruleRunId);
        if (ruleRun == null) {
            return ResponseData.error("没有查询到信息");
        }
        if (!ANALYSIS_COMPLETE_STATUS.equals(ruleRun.getStatus())) {
            return ResponseData.error("只能保存完成分析的数据");
        }
        String ruleId = ruleRun.getRuleId();
        if (StringUtils.isEmpty(ruleId)) {
            return ResponseData.error("模型ID为空");
        }
        boolean result = iExecuteManageService.saveAsModel(ruleId, saveAsModelDto);
        if (!result) {
            return ResponseData.error("保存为模型失败");
        }
        return ResponseData.success();
    }
}
