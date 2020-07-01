package com.ylink.aml.modular.system.controller;

import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ylink.aml.config.properties.GunsProperties;
import com.ylink.aml.core.common.constant.Const;
import com.ylink.aml.modular.system.entity.TargetTabCol;
import com.ylink.aml.modular.system.model.BatchCalcDto;
import com.ylink.aml.modular.system.model.DataAnalysisParamDto;
import com.ylink.aml.modular.system.model.DataStatisticsParamDto;
import com.ylink.aml.modular.system.model.SaveAnalysisAsModelDto;
import com.ylink.aml.modular.system.service.IDataAnalysisService;
import com.ylink.aml.modular.system.service.TargetTabColService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author qy
 */
@Slf4j
@Controller
@RequestMapping(value = "/analysis")

public class DataAnalysisController extends BaseController {

    private static final String PREFIX = "/modular/dataAnalysis/";

    private IDataAnalysisService iDataAnalysisService;

    private TargetTabColService targetTabColService;

    private final GunsProperties gunsProperties;

    @Autowired
    public DataAnalysisController(IDataAnalysisService iDataAnalysisService, TargetTabColService targetTabColService, GunsProperties gunsProperties) {
        this.iDataAnalysisService = iDataAnalysisService;
        this.targetTabColService = targetTabColService;
        this.gunsProperties = gunsProperties;
    }

    @Value("${export.file.path}")
    private String fiePath;

    @Value("${aml.appType}")
    private String appType;

    /**
     * 跳转到数据分析页面
     *
     * @return 页面url
     */
    @RequestMapping("")
    public String index() {
        //大数据版本
        if (Const.APP_VERSION_BIGDATA.equalsIgnoreCase(gunsProperties.getAppVersion())) {
            return PREFIX + "dataAnalysis_bd.html";
        }else if(Const.APP_TYPE_PAY.equalsIgnoreCase(appType)){
            return PREFIX + "dataAnalysis_pay.html";
        } else {
            return PREFIX + "dataAnalysis.html";
        }
    }

    @GetMapping(value = "/findColByTable")
    @ResponseBody
    public List<Map<String, Object>> findColByTable(@RequestParam(required = false, value = "tableName") String tableName) {
        if (StringUtils.isEmpty(tableName)) {
            return new ArrayList<>();
        }
        QueryWrapper<TargetTabCol> wrapper = new QueryWrapper<>();
        wrapper.select("column_name", "column_desc", "data_type", "date_format")
                .eq("table_name", tableName)
                .orderByAsc("column_seq");
        return targetTabColService.listMaps(wrapper);
    }

    @GetMapping(value = "/findDicByCol")
    @ResponseBody
    public Object findDicByCol(@RequestParam(required = false, value = "tableName") String tableName,
                               @RequestParam(required = false, value = "colName") String colName) {
        if (StringUtils.isEmpty(tableName)) {
            return new ArrayList<>();
        }
        if (StringUtils.isEmpty(colName)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> reList = iDataAnalysisService.findDicByCol(tableName, colName);
        if (reList == null || reList.size() < 1) {
            return new ArrayList<>();
        }
        return reList;
    }

    @PostMapping(value = "/getListBySql")
    @ResponseBody
    public Object getListBySql(@RequestBody DataAnalysisParamDto paramDto) {
        if (paramDto == null) {
            return null;
        }
        Map<String, Object> reMap = iDataAnalysisService.getListBySql(paramDto);
        if (reMap != null && reMap.size() > 0) {
            return reMap;
        }
        return Collections.EMPTY_MAP;
    }

    @PostMapping(value = "/saveAsModel")
    @ResponseBody
    public Object saveAsModel(@RequestBody SaveAnalysisAsModelDto analysis) {
        if (analysis == null) {
            return ResponseData.error("参数为空");
        }
        int insertResult = iDataAnalysisService.saveAnalysisAsModel(analysis);
        if (insertResult > 0) {
            return SUCCESS_TIP;
        }
        return ResponseData.error("保存失败");
    }

    @PostMapping(value = "/exportData")
    @ResponseBody
    public void exportData(@RequestBody DataAnalysisParamDto paramDto, HttpServletResponse response) {
        if (paramDto == null) {
            return;
        }
        File file = iDataAnalysisService.exportData(paramDto);
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }
        response.setContentType("text/csv;charset=UTF-8");
        applyFile(file, response, true);
    }

    @PostMapping(value = "/statistics")
    @ResponseBody
    public Object statistics(@RequestBody DataStatisticsParamDto paramDto) {
        if (paramDto == null) {
            return Collections.EMPTY_LIST;
        }
        List<Map<String, Object>> list = iDataAnalysisService.dataStatistics(paramDto);
        if (list == null || list.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        return list;
    }

    @PostMapping(value = "/batch_calc")
    @ResponseBody
    public Object batchCalc(@RequestBody BatchCalcDto calcDto) {
        if (calcDto == null) {
            return Collections.EMPTY_LIST;
        }
        return iDataAnalysisService.batchCalc(calcDto);
    }

    @PostMapping(value = "/exportCalc")
    @ResponseBody
    public void exportCalc(@RequestBody BatchCalcDto calcDto, HttpServletResponse response) {
        if (calcDto == null) {
            return;
        }
        File file = iDataAnalysisService.exportCalc(calcDto);
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }
        response.setContentType("application/x-xls;charset=UTF-8");
        applyFile(file, response, false);
    }


    @PostMapping(value = "/exportCalcRes")
    @ResponseBody
    public void exportCalcRes(@RequestBody DataStatisticsParamDto paramDto, HttpServletResponse response) {
        if (paramDto == null) {
            return;
        }
        File file = iDataAnalysisService.exportCalcRes(paramDto);
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }
        response.setContentType("application/x-xls;charset=UTF-8");
        applyFile(file, response, false);
    }

    @PostMapping(value = "/commitAnalysis")
    @ResponseBody
    public Object commitAnalysis(@RequestBody SaveAnalysisAsModelDto analysis) {
        if (analysis == null) {
            return ResponseData.error("参数不能为空");
        }
        boolean saved = iDataAnalysisService.commitAnalysis(analysis);
        if (saved) {
            return ResponseData.success();
        }
        return ResponseData.error("提交分析失败");
    }
    private void applyFile(File file, HttpServletResponse response, boolean isCsvFile) {
        String fileName = file.getName();
        try {

            response.setHeader("Content-Disposition", "attachment;filename="
                    + URLEncoder.encode(fileName, "UTF-8"));
            FileInputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = response.getOutputStream();
            if (isCsvFile) {
                //加上这个 csv文件用Excel打开就不会乱码
                outputStream.write(new byte []{( byte ) 0xEF ,( byte ) 0xBB ,( byte ) 0xBF });
            }
            byte[] buffer = new byte[1024];
            int i;
            while ((i = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, i);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            log.error("响应到前端失败，{}", e.getMessage());
            e.printStackTrace();
        } finally {
            boolean delete = file.delete();
            if (delete) {
                log.info("文件删除成功");
            }
        }
    }
}
