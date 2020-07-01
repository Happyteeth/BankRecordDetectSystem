package com.ylink.aml.modular.system.controller;

import cn.hutool.core.map.MapUtil;
import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.ylink.aml.modular.system.entity.DataReport;
import com.ylink.aml.modular.system.entity.TargetTab;
import com.ylink.aml.modular.system.model.ExportReportParamsDto;
import com.ylink.aml.modular.system.service.IDataReportService;
import com.ylink.aml.modular.system.service.ITargetTabService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author qy
 */
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/report")
public class DataReportController extends BaseController {
    private static final String PREFIX = "/modular/dataReport/";

    private ITargetTabService iTargetTabService;

    private IDataReportService iDataReportService;

    /**
     * 跳转到数据报表页面界面
     * @return java.lang.String
     */
    @RequestMapping("")
    public String index() {
        return PREFIX + "dataReport.html";
    }

    @RequestMapping(value = "/tableList")
    @ResponseBody
    public List<Map<String, Object>> tableNameList() {
        QueryWrapper<TargetTab> wrapper = new QueryWrapper<>();
        wrapper.select("table_name, table_desc");
        return iTargetTabService.listMaps(wrapper);
    }

    @RequestMapping(value = "/hasReportTableList")
    @ResponseBody
    public Object hasReportTableList() {
        QueryWrapper<TargetTab> wrapper = new QueryWrapper<>();
        wrapper.select("table_name", "table_desc")
                .inSql("TABLE_NAME", "SELECT DISTINCT TABLE_NAME FROM DATA_REPORT");
        List<Map<String, Object>> list = iTargetTabService.listMaps(wrapper);
        Map<String, Object> reMap = MapUtil.newHashMap();
        reMap.put("data", list);
        reMap.put("time", iDataReportService.findReportDate());
        return reMap;
    }

    @RequestMapping(value = "/getReportByTable")
    @ResponseBody
    public Object getReportByTable(@RequestParam(required = false, value = "tableName") String tableName) {
        if (StringUtils.isEmpty(tableName)) {
            return null;
        }
        return iDataReportService.getReportByTableName(tableName);
    }

    @RequestMapping(value = "/exportReport")
    @ResponseBody
    public void exportReport(@RequestBody ExportReportParamsDto params, HttpServletResponse response) {
        if (params == null) {
            log.error("数据为空");
            return;
        }
        String tableName = params.getTableName();
        List<Map<String, String>> chartCodeList = params.getChartTypes();
        if (StringUtils.isEmpty(tableName)) {
            log.error("表名为空 : {}", params);
            return;
        }
        if (chartCodeList == null || chartCodeList.size() < 1) {
            log.error("图表信息为空 : {}", params);
            return;
        }
        iDataReportService.exportReport(params, response);
    }


}
