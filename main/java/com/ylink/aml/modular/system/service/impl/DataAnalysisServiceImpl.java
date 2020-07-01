package com.ylink.aml.modular.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ylink.aml.core.shiro.ShiroKit;
import com.ylink.aml.modular.system.entity.*;
import com.ylink.aml.modular.system.mapper.*;
import com.ylink.aml.modular.system.model.*;
import com.ylink.aml.modular.system.service.IDataAnalysisService;
import com.ylink.aml.modular.system.service.RuleRunInsertService;
import com.ylink.aml.modular.system.service.TbsProgParaService;
import com.ylink.aml.modular.system.util.CsvUtils;
import com.ylink.aml.modular.system.util.ExcelUtils;
import com.ylink.aml.modular.system.util.sqlParse.SQLParse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author qy
 */
@Service
@Slf4j
public class DataAnalysisServiceImpl implements IDataAnalysisService {

    private TargetTabColMapper targetTabColMapper;

    private TbsDictionaryValMapper tbsDictionaryValMapper;

    private ModelMapper modelMapper;

    private DataAnalysisMapper dataAnalysisMapper;

    private RuleTargetMappingMapper ruleTargetMappingMapper;

    private RuleRunInsertService ruleRunInsertService;

    private final RuleDefMapper ruleDefMapper;
    private TbsProgParaService tbsProgParaService;
    private static String driverName = "org.apache.hive.jdbc.HiveDriver";
    @Value("${aml.appType}")
    private String appType;

    private static final String EXCEL_PATH = "excel/";

    private static final String IMG_PATH = "img/";

    private static final String EXCEL_SUFFIX = ".xlsx";

    private static final String IMG_SUFFIX = ".png";

    private static final String[] CALC_ARR = {"SUM", "COUNT", "AVG", "MAX", "MIN"};

    public DataAnalysisServiceImpl(TargetTabColMapper targetTabColMapper, TbsDictionaryValMapper tbsDictionaryValMapper, ModelMapper modelMapper,
                                   DataAnalysisMapper dataAnalysisMapper, RuleTargetMappingMapper ruleTargetMappingMapper,
                                   RuleRunInsertService ruleRunInsertService, RuleDefMapper ruleDefMapper, TbsProgParaService tbsProgParaService) {
        this.targetTabColMapper = targetTabColMapper;
        this.tbsDictionaryValMapper = tbsDictionaryValMapper;
        this.modelMapper = modelMapper;
        this.dataAnalysisMapper = dataAnalysisMapper;
        this.ruleTargetMappingMapper = ruleTargetMappingMapper;
        this.ruleRunInsertService = ruleRunInsertService;
        this.ruleDefMapper = ruleDefMapper;
        this.tbsProgParaService = tbsProgParaService;
    }

    @Override
    public List<Map<String, Object>> findDicByCol(String tableName, String colName) {
        QueryWrapper<TargetTabCol> colWrapper = new QueryWrapper<>();
        colWrapper.select("v_item_id")
                .eq("table_name", tableName)
                .eq("column_name", colName);
        List<Object> itemIdList = targetTabColMapper.selectObjs(colWrapper);
        if (itemIdList == null || itemIdList.size() < 1) {
            return null;
        }
        Object obj = itemIdList.get(0);
        if (obj == null || obj == "") {
            return null;
        }
        String vItemId = obj.toString();
        QueryWrapper<TbsDictionaryVal> dicWrapper = new QueryWrapper<>();
        dicWrapper.select("v_item_val_id", "v_val_name")
                .eq("v_item_id", vItemId);
        return tbsDictionaryValMapper.selectMaps(dicWrapper);
    }

    @Override
    public Map<String, Object> getListBySql(DataAnalysisParamDto paramDto) {
        //查询数量SQL
        String countSql = getQuerySql(true, paramDto, null);
        if (StringUtils.isEmpty(countSql)) {
            return null;
        }
        log.info("获取到的查询数据数量SQL为：{}", countSql);
        int count = dataAnalysisMapper.customCountSql(countSql);
        if (count <= 0) {
            return null;
        }
        //查询列表SQL
        String sql = getQuerySql(false, paramDto, null);
        if (StringUtils.isEmpty(sql)) {
            return null;
        }
        sql += " LIMIT 100";
        log.info("获取到的查询数据列表SQL为：{}", sql);
        List<Map<String, Object>> list = dataAnalysisMapper.customListSql(sql);
        Map<String, Object> map = new HashMap<>();
        map.put("count", count);
        if (list != null && list.size() > 0) {
            List<LinkedHashMap<String, Object>> reList = orderByResult(list, paramDto.getColumnList());
            map.put("data", reList);
        } else {
            map.put("data", Collections.EMPTY_LIST);
        }
        return map;
    }

    private List<LinkedHashMap<String, Object>> orderByResult(List<Map<String, Object>> list, List<String> columnList) {
        List<LinkedHashMap<String, Object>> reList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
            for (String column : columnList) {
                dataMap.put(column, map.get(column));
            }
            reList.add(dataMap);
        }
        return reList;
    }

    @Override
    public File exportData(DataAnalysisParamDto paramDto) {
        List<String> chColumnList = paramDto.getChColumnList();
        String sql = getQuerySql(false, paramDto, chColumnList);
        if (StringUtils.isEmpty(sql)) {
            log.error("获取到的sql为空，查询失败，查询参数：{}", paramDto);
            return null;
        }
        log.info("获取到的数据SQL为：{}", sql);
        List<Map<String, Object>> list = dataAnalysisMapper.customListSql(sql);
        if (list == null || list.size() < 1) {
            return null;
        }
        List<String> dataList = new ArrayList<>();
        //先获取标题
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = list.get(i);
            if (i == 0) {
                String title = "";
                for (String key : map.keySet()) {
                    if (StringUtils.isNotEmpty(title)) {
                        title += ",";
                    }
                    if (chColumnList.contains(key)) {
                        title += key;
                    }
                }
                dataList.add(title);
            }
            String dataVal = "";
            int index = 0;
            for (Object obj : map.values()) {
                String value = obj == null ? "" : obj.toString();
                if (index > 0) {
                    dataVal += ",";
                }
                if (StringUtils.isNotEmpty(value)) {
                    dataVal += "\t";
                }
                dataVal += value;
                index++;
            }
            if (StringUtils.isNotEmpty(dataVal)) {
                dataList.add(dataVal);
            }
        }
        String basePath = System.getProperty("user.dir");
        log.error("获取导的系统根目录为： {}", basePath);
        File fileDir = new File(basePath);
        //如果文件夹不存在，则创建新的文件夹
        if (!fileDir.exists()) {
            boolean mkdirs = fileDir.mkdirs();
            if (!mkdirs) {
                log.error("创建根目录失败，basePath {}", basePath);
                return null;
            }
        }
        String curFilePath = basePath + "/" + paramDto.getFileName();
        File file = new File(curFilePath);
        boolean success = CsvUtils.exportCsv(file, dataList);
        if (success) {
            return file;
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> dataStatistics(DataStatisticsParamDto paramDto) {
        String statisticsSql = getStatisticsSql(paramDto);
        if (StringUtils.isEmpty(statisticsSql)) {
            log.error("获取到的统计sql为空，统计失败：param： {}", paramDto);
            return null;
        }
        log.info("获取到的图表SQL为：{}", statisticsSql);
        List<Map<String, Object>> reList = dataAnalysisMapper.customListSql(statisticsSql);
        return reList;
    }

    @Override
    public Map<String, List<Map<String, Object>>> batchCalc(BatchCalcDto calcDto) {
        Map<String, List<Map<String, Object>>> reMap = MapUtil.newHashMap();
        if (calcDto == null) {
            return reMap;
        }
        if (StringUtils.isEmpty(calcDto.getTableName())) {
            return reMap;
        }
        if (calcDto.getList() == null || calcDto.getList().size() < 1) {
            return reMap;
        }
        List<DataStatisticsParamDto> paramDtoList = orgCalcParam(calcDto);
        if (paramDtoList.size() > 0) {
            for (DataStatisticsParamDto paramDto : paramDtoList) {
                List<Map<String, Object>> dataList = dataStatistics(paramDto);
                reMap.put(paramDto.getGroupBy(), dataList);
            }
        }
        return reMap;
    }

    @Override
    public File exportCalcRes(DataStatisticsParamDto paramDto) {
        String chGroupBy = paramDto.getChGroupBy();
        String groupBy = paramDto.getGroupBy();
        if (StringUtils.isEmpty(chGroupBy) || StringUtils.isEmpty(groupBy)) {
            return null;
        }
        List<Map<String, Object>> dataList = dataStatistics(paramDto);
        if (dataList == null || dataList.size() < 1) {
            return null;
        }
        String fileName = chGroupBy + "_" + System.currentTimeMillis();
        //将数据添加到Excel
        String basePath = System.getProperty("user.dir");
        log.error("获取导的系统根目录为： {}", basePath);
        String excelDirPath = basePath + "/" + EXCEL_PATH;
        File excelDir = new File(excelDirPath);
        if (!excelDir.exists() || !excelDir.isDirectory()) {
            excelDir.mkdirs();
        }
        String excelFilePath = excelDirPath + fileName + EXCEL_SUFFIX;
        File excelFile = new File(excelFilePath);
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("数据分析计算");
            //如果这行没有了，整个公式都不会有自动计算的效果的
            sheet.setForceFormulaRecalculation(true);
            //设置表头
            //将key提取出来设置表头
            List<String> keyList = new ArrayList<>();
            Map<String, Object> dataMap = dataList.get(0);
            for (String key : dataMap.keySet()) {
                keyList.add(key);
            }
            //设置表头
            setTitle(sheet, keyList, chGroupBy, groupBy);
            //赋值
            for (int i = 0; i < dataList.size(); i++) {
                //第二行开始，第一行设置标题
                evaluateDataToExcel(sheet, i + 1, dataList.get(i));
            }
            //将eCharts图表存入到Excel
            String imgDirPath = basePath + "/" + IMG_PATH;
            File imgDir = new File(imgDirPath);
            if (!imgDir.exists() || !imgDir.isDirectory()) {
                imgDir.mkdirs();
            }
            String imgFilePath = imgDirPath + fileName + IMG_SUFFIX;
            boolean success = ExcelUtils.evaluateReportToExcel(workbook, sheet, paramDto.getChartCode(), imgFilePath, dataList.size());
            if (!success) {
                log.error("导出Excel报表失败");
                return null;
            }
            workbook.write(new FileOutputStream(excelFile));
            workbook.close();
        } catch (Exception e) {
            log.error("生成Excel文件失败：{}", e.getMessage());
            return null;
        }
        return excelFile;
    }

    @Override
    public File exportCalc(BatchCalcDto calcDto) {
        if (calcDto == null) {
            return null;
        }
        String chartCode = calcDto.getChartCode();
        if (StringUtils.isEmpty(chartCode)) {
            return null;
        }
        List<DataStatisticsParamDto> paramDtoList = orgCalcParam(calcDto);
        if (paramDtoList.size() < 1) {
            return null;
        }
        String fileName = "报表数据" + "_" + System.currentTimeMillis();
        //将数据添加到Excel
        String basePath = System.getProperty("user.dir");
        log.error("获取导的系统根目录为： {}", basePath);
        String excelDirPath = basePath + "/" + EXCEL_PATH;
        File excelDir = new File(excelDirPath);
        if (!excelDir.exists() || !excelDir.isDirectory()) {
            excelDir.mkdirs();
        }
        String excelFilePath = excelDirPath + fileName + EXCEL_SUFFIX;
        File excelFile = new File(excelFilePath);
        ExcelWriter writer = ExcelUtil.getWriter(excelFile, "报表数据");
        for (DataStatisticsParamDto paramDto : paramDtoList) {
            String chGroupBy = paramDto.getChGroupBy();
            String groupBy = paramDto.getGroupBy();
            if (StringUtils.isEmpty(chGroupBy) || StringUtils.isEmpty(groupBy)) {
                continue;
            }
            List<Map<String, Object>> dataList = dataStatistics(paramDto);
            if (dataList == null || dataList.size() < 1) {
                continue;
            }
            writer.write(dataList, true);
        }
        //将eCharts图表存入到Excel
        String imgDirPath = basePath + "/" + IMG_PATH;
        File imgDir = new File(imgDirPath);
        if (!imgDir.exists() || !imgDir.isDirectory()) {
            imgDir.mkdirs();
        }
        String imgFilePath = imgDirPath + fileName + IMG_SUFFIX;

        boolean success = ExcelUtils.evaluateReportToExcel(writer.getWorkbook(), writer.setSheet("报表图表").getSheet(), chartCode, imgFilePath, 1);
        if (!success) {
            log.error("导出Excel报表失败");
            return null;
        }
        writer.close();
        return excelFile;
    }

    @Override
    public int saveAnalysisAsModel(SaveAnalysisAsModelDto analysis) {
        DataAnalysisParamDto analysisParam = new DataAnalysisParamDto();
        BeanUtils.copyProperties(analysis, analysisParam);
        DataStatisticsParamDto statisticsParam = new DataStatisticsParamDto();
        BeanUtils.copyProperties(analysis, statisticsParam);
        String listSql = getQuerySql(false, analysisParam, analysis.getChColumnList());
        if (StringUtils.isEmpty(listSql)) {
            log.error("查询列表sql为空，{}", analysisParam);
            return 0;
        }
        String chartSql = getStatisticsSql(statisticsParam);
        Model model = new Model();
        BeanUtils.copyProperties(analysis, model);
        //设置执行语言 此处设置为SQL
        model.setTaskProgram("0");
        //设置数据SQL
        model.setRuleProg(listSql);
        //设置图表SQL
        model.setChartProg(chartSql);
        //设置规则分析，此处为模型
        model.setRuleType("2");
        //删除标志设置为 未删除
        model.setCDelFlag("0");
        //设置模型涞源，此处为数据分析保存的
        model.setModelSource("1");
        //设置不参与一键检测
        model.setIfAutoCheck("0");
        //设置图表的X轴对应的字段别名 改为前端传值
        String account = null;
        if (ShiroKit.getUser() != null) {
            account = ShiroKit.getUser().getAccount();
        }
        model.setVInsertUser(account);

        String countSql = getQuerySql(true, analysisParam, analysis.getChColumnList());
        if (StringUtils.isEmpty(countSql)) {
            log.error("计数SQL查询失败");
            return 0;
        }
        model.setRuleCount(countSql);
        int insertResult = modelMapper.insert(model);
        if (insertResult > 0) {
            String ruleId = model.getRuleId();
            if (StringUtils.isNotEmpty(ruleId)) {
                //保存关联关系
                RuleTargetMapping ruleTargetMapping = new RuleTargetMapping();
                ruleTargetMapping.setRuleId(ruleId);
                ruleTargetMapping.setTableName(analysis.getTableName());
                ruleTargetMappingMapper.insert(ruleTargetMapping);
            }
        }
        return insertResult;
    }

    @Override
    public boolean commitAnalysis(SaveAnalysisAsModelDto analysis) {
        if (analysis == null) {
            log.error("参数为空，提交分析失败");
            return false;
        }
        DataAnalysisParamDto analysisParam = new DataAnalysisParamDto();
        BeanUtils.copyProperties(analysis, analysisParam);
        DataStatisticsParamDto statisticsParam = new DataStatisticsParamDto();
        BeanUtils.copyProperties(analysis, statisticsParam);
        String listSql = getQuerySql(false, analysisParam, analysis.getChColumnList());
        if (StringUtils.isEmpty(listSql)) {
            log.error("查询列表sql为空，{}", analysisParam);
            return false;
        }
        String url = tbsProgParaService.selectParaValue("DATA_FILE_UPLOAD", "HIVE_URL");
        log.info(url);
        boolean isSql = SQLParse.hiveSQLParse(url, driverName, listSql);
        if (!isSql) {
            log.error("列表SQL语法出错，无法提交, sql : {}", listSql);
            return false;
        }
        log.info("获取到的分析列表SQL为：{}", listSql);
        String chartSql = getStatisticsSql(statisticsParam);
        if (StringUtils.isEmpty(chartSql)) {
            //图表SQL为空可以提交
            log.error("查询图表sql为空，{}", statisticsParam);
//            return false;
        } else {
            //boolean isChartSql = SQLParse.parse(chartSql, "hive");
            boolean isChartSql = SQLParse.hiveSQLParse(url, driverName, chartSql);
            if (!isChartSql) {
                log.error("图表SQL语法出错，无法提交：sql {}", isChartSql);
                return false;
            }
        }
        Model model = new Model();
        BeanUtils.copyProperties(analysis, model);
        //设置执行语言 此处设置为SQL
        model.setTaskProgram("0");
        //设置数据SQL
        model.setRuleProg(listSql);
        //设置图表SQL
        model.setChartProg(chartSql);
        //设置规则分析，此处为数据分析
        model.setRuleType("1");
        //设置模型涞源，此处为数据分析保存的
        model.setModelSource("1");
        //删除标志设置为 未删除
        model.setCDelFlag("0");
        //设置图表的X轴对应的字段别名 改为前端传值
        String account = null;
        if (ShiroKit.getUser() != null) {
            account = ShiroKit.getUser().getAccount();
        }
        model.setVInsertUser(account);
        int insertRes = modelMapper.insert(model);
        if (insertRes < 1) {
            log.error("保存分析数据失败");
            return false;
        }
        String ruleId = model.getRuleId();
        if (StringUtils.isEmpty(ruleId)) {
            log.error("获取模型ID失败，保存分析数据失败");
            return false;
        }
        String ruleRunId = ruleRunInsertService.createRuleRun(model, 0);
        if (StringUtils.isEmpty(ruleRunId)) {
            log.error("生成执行实例失败");
            return false;
        }
        return true;
    }

    private List<DataStatisticsParamDto> orgCalcParam(BatchCalcDto calcDto) {
        List<DataStatisticsParamDto> paramDtoList = new ArrayList<>();
        for (CalcDto dto : calcDto.getList()) {
            DataStatisticsParamDto paramDto = new DataStatisticsParamDto();
            if (StringUtils.isEmpty(dto.getGroupBy()) || dto.getCalcMap() == null || dto.getCalcMap().length < 1) {
                continue;
            }
            paramDto.setTableName(calcDto.getTableName());
            paramDto.setConditionList(calcDto.getConditionList());
            paramDto.setRelation(calcDto.getRelation());
            paramDto.setCalcMap(dto.getCalcMap());
            paramDto.setGroupBy(dto.getGroupBy());
            paramDto.setChGroupBy(dto.getChGroupBy());
            paramDtoList.add(paramDto);
        }
        return paramDtoList;
    }

    private void setTitle(XSSFSheet sheet, List<String> keyList, String chGroupBy, String groupBy) {
        XSSFRow titleRow = sheet.createRow(0);
        for (int i = 0; i < keyList.size(); i++) {
            XSSFCell cell = titleRow.createCell(i);
            String key = keyList.get(i);
            if (groupBy.equals(key)) {
                cell.setCellValue(chGroupBy);
            } else {
                cell.setCellValue(key);
            }
        }
    }

    private void evaluateDataToExcel(XSSFSheet sheet, int rowNum, Map<String, Object> dataMap) {
        //获取所有的value
        List<Object> valueList = new ArrayList<>(dataMap.values());
        //获取当前行
        XSSFRow row = sheet.createRow(rowNum);
        //将valueList添加到Excel
        for (int i = 0; i < valueList.size(); i++) {
            //将数据分别添加到每一列中
            XSSFCell cell = row.createCell(i);
            Object value = valueList.get(i);
            if (value != null && !"".equals(value)) {
                cell.setCellValue(String.valueOf(value));
            }
        }
    }

    private String getStatisticsSql(DataStatisticsParamDto paramDto) {
        String tableName = paramDto.getTableName();
        if (StringUtils.isEmpty(tableName)) {
            log.error("统计表名为空，统计失败， param：{}", paramDto);
            return null;
        }
        String groupBy = paramDto.getGroupBy();
        if (StringUtils.isEmpty(groupBy)) {
            log.error("汇总条件为空，统计失败，param： {}", paramDto);
            return null;
        }
        String chGroupBy = paramDto.getChGroupBy();
        Map<String, String>[] calcMap = paramDto.getCalcMap();
        if (calcMap == null || calcMap.length < 1) {
            log.error("统计参数为空，统计失败，param： {}", paramDto);
            return null;
        }
        List<ConditionDto> conditionList = paramDto.getConditionList();
        String relationType = paramDto.getRelation();
        if (conditionList != null && conditionList.size() > 1 && StringUtils.isEmpty(relationType)) {
            log.error("多个查询条件但条件关系为空，param：{}", paramDto);
            return null;
        }
        //根据分组字段判断是否为字典表
        QueryWrapper<TargetTabCol> wrapper = new QueryWrapper<>();
        wrapper.select("column_name", "v_item_id")
                .eq("table_name", tableName)
                .eq("column_name", groupBy)
                .isNotNull("v_item_id");
        List<Map<String, Object>> dicColumnList = targetTabColMapper.selectMaps(wrapper);
        String vItemId = "";
        if (dicColumnList != null && dicColumnList.size() > 0) {
            vItemId = dicColumnList.get(0).get("v_item_id") == null ? "" : dicColumnList.get(0).get("v_item_id").toString();
        }
        StrBuilder builder = StrBuilder.create();
        if (StringUtils.isEmpty(vItemId)) {
            builder.append("SELECT").append(" t.").append(groupBy);
        } else {
            builder.append("SELECT").append(" t1.V_VAL_NAME");
        }
        if (StringUtils.isNotEmpty(chGroupBy)) {
            builder.append(" AS `").append(chGroupBy).append("`");
        } else {
            builder.append(" AS `").append(groupBy).append("`");
        }
        for (Map<String, String> map : calcMap) {
            String key = map.get("key");
            String value = map.get("value");
            String calcType = key.substring(0, key.indexOf("("));
            if (Arrays.asList(CALC_ARR).contains(calcType)) {
                builder.append(", ")
                        .append(calcType)
                        .append("(t.")
                        .append(value)
                        .append(") AS ")
                        .append("`").append(key).append("`");
            }
        }
        builder.append(" FROM ").append(tableName).append(" t");
        if (StringUtils.isNotEmpty(vItemId)) {
            builder.append("  LEFT JOIN TBS_DICTIONARY_VAL t1 ON t1.V_ITEM_ID = ").append(vItemId)
                    .append(" AND t1.V_ITEM_VAL_ID = t.").append(groupBy);
        }
        if (conditionList != null && conditionList.size() > 0) {
            builder.append(getConditionSql(conditionList, relationType, tableName));
        }
        if (StringUtils.isEmpty(vItemId)) {
            builder.append(" GROUP BY t.").append(groupBy);
        } else {
            builder.append(" GROUP BY t1.V_VAL_NAME");
        }
        return builder.toString();
    }

    private String getQuerySql(boolean isCountSql, DataAnalysisParamDto paramDto, List<String> chColumnList) {
        String tableName = paramDto.getTableName();
        List<String> columnList = paramDto.getColumnList();
        List<ConditionDto> conditionList = paramDto.getConditionList();
        String relationType = paramDto.getRelation();
        if (StringUtils.isEmpty(tableName)) {
            log.error("查询表名为空，查询失败");
            return null;
        }
        if (columnList == null || columnList.size() < 1) {
            log.error("查询列名为空，查询失败");
            return null;
        }
        if (conditionList != null && conditionList.size() > 1 && StringUtils.isEmpty(relationType)) {
            log.error("多个查询条件，查询关系为空，查询失败");
            return null;
        }

        StringBuilder sql = new StringBuilder();
        if (isCountSql) {
            sql.append("SELECT COUNT(*) FROM ").append(tableName).append(" t");
        } else {
            Map<String, LinkedHashMap<String, String>> columnDicMap = getColumnSql(tableName, columnList, chColumnList);
            String columnSql = "";
            LinkedHashMap<String, String> relationMap = null;
            for (Map.Entry<String, LinkedHashMap<String, String>> entry : columnDicMap.entrySet()) {
                columnSql = entry.getKey();
                relationMap = entry.getValue();
            }
            sql.append("SELECT")
                    .append(" ")
                    .append(columnSql)
                    .append(" ")
                    .append("FROM")
                    .append(" ")
                    .append(tableName).append(" t");
            if (relationMap != null && relationMap.size() > 0) {
                int i = 0;
                for (Map.Entry<String, String> entry : relationMap.entrySet()) {
                    sql.append(" LEFT JOIN TBS_DICTIONARY_VAL")
                            .append(" t").append(i)
                            .append(" ON t").append(i).append(".V_ITEM_ID = ").append(entry.getValue())
                            .append(" AND t").append(i).append(".V_ITEM_VAL_ID = ").append("t.").append(entry.getKey());
                    i++;
                }
            }
        }
        if (conditionList != null && conditionList.size() > 0) {
            sql.append(getConditionSql(conditionList, relationType, tableName));
        }
        return sql.toString();
    }

    private Map<String, LinkedHashMap<String, String>> getColumnSql(String tableName, List<String> columnList, List<String> chColumnList) {
        Map<String, LinkedHashMap<String, String>> reMap = new HashMap<>();
        String columnSql = "";
        QueryWrapper<TargetTabCol> wrapper = new QueryWrapper<>();
        wrapper.select("column_name", "v_item_id")
                .eq("table_name", tableName)
                .in("column_name", columnList)
                .isNotNull("v_item_id");
        List<Map<String, Object>> dicColumnList = targetTabColMapper.selectMaps(wrapper);
        if (dicColumnList == null || dicColumnList.size() == 0) {
            if (chColumnList != null && chColumnList.size() > 0) {
                for (int i = 0; i < columnList.size(); i++) {
                    if (i != 0) {
                        columnSql += ", ";
                    }
                    columnSql += "t." + columnList.get(i) + " AS `" + chColumnList.get(i) + "`";
                }
            } else {
                columnSql = StringUtils.join(columnList.toArray(), ", t.");
                columnSql = "t." + columnSql;
            }
            reMap.put(columnSql, new LinkedHashMap<>());
            return reMap;
        }
        LinkedHashMap<String, String> relationMap = new LinkedHashMap<>();
        List<String> dicList = new ArrayList<>();
        for (int i = 0; i < dicColumnList.size(); i++) {
            Map<String, Object> map = dicColumnList.get(i);
            if (map == null || map.size() != 2) {
                continue;
            }
            String columnName = map.get("column_name") == null ? "" : map.get("column_name").toString();
            String vItemId = map.get("v_item_id") == null ? "" : map.get("v_item_id").toString();
            if (StringUtils.isEmpty(columnName) || StringUtils.isEmpty(vItemId)) {
                continue;
            }
            relationMap.put(columnName, vItemId);
            dicList.add(columnName);
            if (i > 0) {
                columnSql += ",";
            }
            if (chColumnList != null && chColumnList.size() > 0 && StringUtils.isNotEmpty(chColumnList.get(columnList.indexOf(columnName)))) {
                columnSql += " t" + i + ".V_VAL_NAME AS `" + chColumnList.get(columnList.indexOf(columnName)) + "`";
            } else {
                columnSql += " t" + i + ".V_VAL_NAME AS `" + columnName + "`";
            }
        }
        if (StringUtils.isNotEmpty(columnSql)) {
            columnSql += ",";
        }
        for (int i = 0; i < columnList.size(); i++) {
            String column = columnList.get(i);
            String chColumn = "";
            if (chColumnList != null && chColumnList.size() > 0) {
                chColumn = chColumnList.get(i);
            }
            if (!columnSql.contains(column) && !dicList.contains(column)) {
                if (StringUtils.isNotEmpty(chColumn) && !columnSql.contains(chColumn)) {
                    columnSql = columnSql + " t." + column + " AS `" + chColumn + "`,";
                } else {
                    columnSql = columnSql + " t." + column + ",";
                }
            }
        }
        columnSql = columnSql.substring(0, columnSql.length() - 1);
        reMap.put(columnSql, relationMap);
        return reMap;
    }

    private String getConditionSql(List<ConditionDto> conditionList, String relation, String tableName) {
        StringBuilder sql = new StringBuilder();
        int index = 0;
        for (ConditionDto condition : conditionList) {
            String conditionType = getConditionType(condition);
            String column = condition.getColumn();
            String value = condition.getValue();
            String dataType = condition.getDataType();
            if (StringUtils.isNotEmpty(dataType) && "T".equals(dataType)) {
                value = formatTimeValue(tableName, column, value);
            }
            if (StringUtils.isEmpty(conditionType) || StringUtils.isEmpty(column)) {
                log.error("该查询条件参数不全，过滤：{}", condition);
                continue;
            }
            if (index == 0) {
                sql.append(" WHERE ");
            } else {
                if (StringUtils.isNotEmpty(relation)) {
                    sql.append(" ").append(relation).append(" ");
                } else {
                    return null;
                }
            }
            switch (conditionType) {
                case "like":
                case "not like":
                    sql.append("t.").append(column).append(" ").append(conditionType).append(" '%").append(value).append("%'");
                    break;
                case "7":
                    sql.append("(t.").append(column).append(" is null")
                            .append(" or t.").append(column).append(" = '@N'")
                            .append(" or t.").append(column).append(" = '')");
                    break;
                case "8":
                    if (StringUtils.isNotEmpty(value)) {
                        value = "('" + StringUtils.join(value.split(","), "', '") + "')";
                        sql.append("t.").append(column).append(" not in").append(" ").append(value);
                    }
                    break;
                default:
                    sql.append("t.").append(column).append(" ").append(conditionType).append(" ");
                    if ("S".equals(dataType) || "T".equals(dataType)) {
                        sql.append("'").append(value).append("'");
                    } else {
                        sql.append(value);
                    }
                    break;
            }
            index++;
        }
        return sql.toString();
    }

    private String formatTimeValue(String tableName, String column, String value) {
        QueryWrapper<TargetTabCol> wrapper = new QueryWrapper<>();
        wrapper.select("DATE_FORMAT")
                .eq("TABLE_NAME", tableName)
                .eq("COLUMN_NAME", column);
        List<Object> list = targetTabColMapper.selectObjs(wrapper);
        String dateFormat = null;
        if (list != null && list.size() > 0) {
            Object obj = list.get(0);
            if (obj != null) {
                dateFormat = obj.toString();
            }
        }
        String val = formatValue(dateFormat, value);
        if (StringUtils.isNotEmpty(val)) {
            if (val.contains("-")) {
                val = val.replaceAll("-", "");
            }
            if (val.contains(":")) {
                val = val.replaceAll(":", "");
            }
            if (val.contains(" ")) {
                val = val.replaceAll(" ", "");
            }
        }
        return val;
    }

    private String formatValue(String dateFormat, String value) {
        if (StringUtils.isEmpty(dateFormat) || StringUtils.isEmpty(value)) {
            return null;
        }
        switch (dateFormat) {
            case "yyyyMMdd":
                dateFormat = "yyyy-MM-dd";
                break;
            case "HHmmss":
                dateFormat = "HH:mm:ss";
                break;
            case "yyyyMMddHHmmss":
                dateFormat = "yyyy-MM-dd HH:mm:ss";
                break;
            default:
                dateFormat = null;
                value = null;
                break;
        }
        if (StringUtils.isEmpty(dateFormat) || StringUtils.isEmpty(value)) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return formatter.format(formatter.parse(value));
    }

    private String getConditionType(ConditionDto condition) {
        String conditionType = null;
        switch (condition.getConditionType()) {
            //大于
            case "1":
                conditionType = ">";
                break;
            case "2":
                conditionType = "<";
                break;
            case "3":
                conditionType = "=";
                break;
            case "4":
                conditionType = ">=";
                break;
            case "5":
                conditionType = "<=";
                break;
            case "6":
                conditionType = "like";
                break;
            case "7":
                conditionType = "7";
                break;
            case "8":
                conditionType = "8";
                break;
            case "9":
                conditionType = "not like";
                break;
            default:
                break;
        }
        return conditionType;
    }

    public static void main(String[] args) {
        Map<String, String> map1 = MapUtil.newHashMap();
        map1.put("姓名", "张三");
        map1.put("年龄", "25");
        map1.put("性别", "男");
        Map<String, String> map2 = MapUtil.newHashMap();
        map2.put("姓名", "李四");
        map2.put("年龄", "28");
        map2.put("性别", "女");
        Map<String, String> map3 = MapUtil.newHashMap();
        map3.put("住址", "张三");
        map3.put("邮箱", "qph@qq.com");
        map3.put("电话", "19900909090");
        ArrayList<Map<String, String>> list = CollUtil.newArrayList(map1, map2);
        // 通过工具类创建writer
        ExcelWriter writer = ExcelUtil.getWriter("/Users/qy/Documents/writeMapTest.xlsx");
        // 一次性写出内容，使用默认样式，强制输出标题
        writer.write(list, true);
        // 关闭writer，释放内存
        list = CollUtil.newArrayList(map3);
        writer.write(list, true);
        writer.close();
    }
}
