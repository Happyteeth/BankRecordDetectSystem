package com.ylink.aml.modular.system.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ylink.aml.modular.system.entity.DataReport;
import com.ylink.aml.modular.system.entity.DataReportDef;
import com.ylink.aml.modular.system.mapper.DataReportDefMapper;
import com.ylink.aml.modular.system.mapper.DataReportMapper;
import com.ylink.aml.modular.system.model.ExportReportParamsDto;
import com.ylink.aml.modular.system.service.IDataReportService;
import com.ylink.aml.modular.system.util.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author qy
 */
@Service
@Slf4j
public class DataReportServiceImpl extends ServiceImpl<DataReportMapper, DataReport> implements IDataReportService {

    private DataReportMapper dataReportMapper;

    private DataReportDefMapper dataReportDefMapper;

    public DataReportServiceImpl(DataReportMapper dataReportMapper, DataReportDefMapper dataReportDefMapper) {
        this.dataReportMapper = dataReportMapper;
        this.dataReportDefMapper = dataReportDefMapper;
    }

    private static final String FILTER_TITLE = "币种及交易量占比";

    private static final String FILTER_SUFFIX = "交易量";

    @Value("${export.file.path}")
    private String fiePath;

    private static final String EXCEL_PATH = "excel/";

    private static final String IMG_PATH = "img/";

    private static final String EXCEL_SUFFIX = ".xlsx";

    private static final String IMG_SUFFIX = ".png";

    @Override
    public Object getReportByTableName(String tableName) {
        QueryWrapper<DataReport> wrapper = new QueryWrapper<>();
        wrapper.select("distinct item_name")
                .eq("table_name", tableName);
        List<Object> itemNameList = dataReportMapper.selectObjs(wrapper);
        if (itemNameList == null || itemNameList.size() < 1) {
            return null;
        }
        List<Map<String, Object>> reList = new ArrayList<>();
        for (Object obj : itemNameList) {
            if (obj != null) {
                Map<String, Object> reMap = MapUtil.newHashMap();
                String itemName = obj.toString();
                wrapper = new QueryWrapper<>();
                wrapper.select("cube_name", "val", "val2")
                        .eq("table_name", tableName)
                        .eq("item_name", itemName)
                        .orderByAsc("cube_name");
                List<DataReport> reportList = dataReportMapper.selectList(wrapper);
                if (reportList == null || reportList.size() < 1) {
                    continue;
                }
                List<String> cubeNameList = new ArrayList<>();
                List<BigDecimal> valList = new ArrayList<>();
                List<BigDecimal> val2List = new ArrayList<>();
                reMap.put("title", itemName);
                reMap.put("divId", UUID.randomUUID().toString());
                //设置图表类型-柱状图 or 饼状图 or 折线图
                reMap.put("type", getCharType(tableName, itemName));
                for (DataReport report : reportList) {
                    String cubeName = report.getCubeName();
                    if (StringUtils.isEmpty(cubeName)) {
                        continue;
                    }
                    if (FILTER_TITLE.equals(itemName) && !cubeName.endsWith(FILTER_SUFFIX)) {
                        continue;
                    }
                    cubeNameList.add(report.getCubeName());
                    BigDecimal val = report.getVal() == null ? new BigDecimal(0) : report.getVal();
                    BigDecimal val2 = report.getVal2() == null ? new BigDecimal(0) : report.getVal2();
                    valList.add(val);
                    //小数转百分比，后台乘100 前端加百分比符号
                    val2List.add(val2.multiply(new BigDecimal(100)));
                }
                reMap.put("xAxis", cubeNameList);
                reMap.put("val", valList);
                reMap.put("val2", val2List);


                reList.add(reMap);
            }
        }
        return reList;
    }

    @Override
    public void exportReport(ExportReportParamsDto params, HttpServletResponse response) {
        if (params == null) {
            log.error("参数为空");
            return;
        }
        String tableName = params.getTableName();
        if (StringUtils.isEmpty(tableName)) {
            log.error("表名为空");
            return;
        }
        String chTableName = params.getChTableName();
        if (StringUtils.isEmpty(chTableName)) {
            log.error("表名描述为空");
            return;
        }
        List<Map<String, String>> chartTypes = params.getChartTypes();
        if (chartTypes == null || chartTypes.size() < 1) {
            log.error("图表信息为空");
            return;
        }
        //将数据添加到Excel
        File excelDir = new File(fiePath + EXCEL_PATH);
        if (!excelDir.exists() || !excelDir.isDirectory()) {
            boolean mkdirs = excelDir.mkdirs();
            if (!mkdirs) {
                log.error("创建文件夹失败");
                return;
            }
        }
        String fileName = chTableName + System.currentTimeMillis();
        String excelFilePath = fiePath + EXCEL_PATH + fileName + EXCEL_SUFFIX;
        File excelFile = new File(excelFilePath);
        ExcelWriter writer = null;
        QueryWrapper<DataReport> wrapper;
        for (int i = 0; i < chartTypes.size(); i++) {
            Map<String, String> chartMap = chartTypes.get(i);
            if (chartMap == null || chartMap.isEmpty()) {
                continue;
            }
            String itemName = chartMap.get("title");
            if (StringUtils.isEmpty(itemName)) {
                continue;
            }
            String chartCode = chartMap.get("chartCode");
            if (StringUtils.isEmpty(chartCode)) {
                continue;
            }
            if (i == 0) {
                writer = ExcelUtil.getWriter(excelFile, itemName.replaceAll("/", "-"));
                //设置所有列为自动宽度，不考虑合并单元格
                writer.autoSizeColumn((short)1);
            }
            wrapper = new QueryWrapper<>();
            wrapper.select("cube_name", "val", "val2")
                    .eq("item_name", itemName)
                    .eq("table_name", tableName)
                    .orderByAsc("cube_name");
            List<DataReport> dataReportList = dataReportMapper.selectList(wrapper);
            if (dataReportList == null || dataReportList.size() < 1) {
                continue;
            }
            if (writer == null) {
                log.error("Excel生成异常");
                return;
            }
            List<LinkedHashMap<String, Object>> reportList = getReportMap(dataReportList);
            int columnLength = reportList.get(0).size();
            if (columnLength <= 0) {
                continue;
            }
            writer.setSheet(itemName.replaceAll("/", "-"));
            writer.merge(columnLength - 1, itemName);
            writer.write(reportList, true);
            //设置eCharts
            Workbook workbook = writer.getWorkbook();
            Sheet sheet = writer.getSheet();
            String imgDirPath = fiePath + IMG_PATH;
            File imgDir = new File(imgDirPath);
            if (!imgDir.exists() || !imgDir.isDirectory()) {
                boolean mkdirs = imgDir.mkdirs();
                if (!mkdirs) {
                    log.error("创建文件夹失败");
                    continue;
                }
            }
            String imgFilePath = imgDirPath + fileName + IMG_SUFFIX;
            boolean result = ExcelUtils.evaluateReportToExcel(workbook, sheet, chartCode, imgFilePath, reportList.size());
            if (!result) {
                log.error("生成失败！！！ itemName : {}, tableName : {}", itemName, tableName);
                writer.close();
                return;
            }
        }
        try {
            //渲染
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename="
                    + URLEncoder.encode(fileName, "UTF-8"));
            if (writer != null) {
                writer.flush(response.getOutputStream());
            }
        } catch (Exception e) {
            log.error("渲染失败");
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

    }

    @Override
    public Date findReportDate() {
        return dataReportMapper.findOneDate();
    }

    private List<LinkedHashMap<String, Object>> getReportMap(List<DataReport> dataReportList) {
        ArrayList<LinkedHashMap<String, Object>> reList = new ArrayList<>();
        for (DataReport report : dataReportList) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("统计项", report.getCubeName());
            map.put("统计值I", report.getVal());
            if (report.getVal2() != null) {
                map.put("统计值II", report.getVal2());
            }
            reList.add(map);
        }
        return reList;
    }

    private String getCharType(String tableName, String itemName) {
        QueryWrapper<DataReportDef> wrapper = new QueryWrapper<>();
        wrapper.eq("table_name", tableName)
                .eq("item_name", itemName);
        List<DataReportDef> defList = dataReportDefMapper.selectList(wrapper);
        if (defList == null || defList.size() < 1) {
            return null;
        }
        return defList.get(0).getChartType();
    }

}
