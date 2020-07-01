package com.ylink.aml.modular.system.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ylink.aml.modular.system.dto.ModelResultData;
import com.ylink.aml.modular.system.entity.Model;
import com.ylink.aml.modular.system.entity.RuleRunViewEntity;
import com.ylink.aml.modular.system.mapper.ModelMapper;
import com.ylink.aml.modular.system.service.IDetectService;
import com.ylink.aml.modular.system.service.RuleRunViewService;
import com.ylink.aml.modular.system.util.ExcelUtils;
import com.ylink.aml.modular.system.util.chart.CreateChartUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author qy
 */
@Service
@Slf4j
public class DetectServiceImpl implements IDetectService {

    public DetectServiceImpl(ModelMapper modelMapper, RuleRunViewService ruleRunViewService) {
        this.modelMapper = modelMapper;
        this.ruleRunViewService = ruleRunViewService;
    }

    private ModelMapper modelMapper;

    private RuleRunViewService ruleRunViewService;

    private static final String DEFAULT_CHART_X = "f0";

    private static final String DEFAULT_CHART_VAL = "f0";

    private static final String DEFAULT_CHART_TYPE = "1";

    private static final String IMG_DIR = "img/";


//    @Value("${export.file.path}")
//    private String fiePath;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");


    @Override
    public File batchExport(String autoCheckId, HttpServletResponse response) {
        if (StringUtils.isEmpty(autoCheckId)) {
            log.error("参数为空，导入失败");
            return null;
        }
        QueryWrapper<RuleRunViewEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("AUTO_CHECK_ID", autoCheckId);
        List<RuleRunViewEntity> viewList = ruleRunViewService.list(wrapper);
        if (viewList == null || viewList.size() < 1) {
            log.error("查询结果为空，参数ID有误，ID：{}", autoCheckId);
            return null;
        }
        String basePath = System.getProperty("user.dir");
        log.error("获取导的系统根目录为： {}", basePath);
        String baseDirPath = basePath + "/" + autoCheckId + "_" + FORMATTER.format(LocalDateTime.now());
        File baseDirFile = new File(baseDirPath);
        if (!baseDirFile.exists() || !baseDirFile.isDirectory()) {
            baseDirFile.mkdirs();
        }
        viewList.forEach(ruleRun -> {
            String chartData = ruleRun.getRerultChartData();
            String oldDataFilePath = ruleRun.getRerultPath();
            if (StringUtils.isEmpty(chartData) && StringUtils.isEmpty(oldDataFilePath)) {
                log.error("图表数据和 原数据都为空，无法导出 ruleRun -> {}", ruleRun);
            }
            String ruleId = ruleRun.getRuleId();
            Model model = modelMapper.selectById(ruleId);
            if (model == null) {
                log.error("关联模型为空，ruleId：{}", ruleId);
                return;
            }
            //模型名称
            String modelName = model.getRuleName();
            String chartX = model.getChartX();
            if (StringUtils.isEmpty(chartX)) {
                chartX = DEFAULT_CHART_X;
            }
            String chartVal = model.getChartVal();
            if (StringUtils.isEmpty(chartVal)) {
                chartVal = DEFAULT_CHART_VAL;
            }
            String chartType = model.getChartType();
            if (StringUtils.isEmpty(chartType)) {
                chartType = DEFAULT_CHART_TYPE;
            }
            String currentDirPath = baseDirPath + "/";
            if (!StringUtils.isEmpty(modelName)) {
                currentDirPath += modelName + "_";
            }
            currentDirPath += ruleId;
            File currentDirFile = new File(currentDirPath);
            if (!currentDirFile.exists() || !currentDirFile.isDirectory()) {
                currentDirFile.mkdirs();
            }
            //数据不为空 ，导出数据
            if (StringUtils.isNotEmpty(oldDataFilePath)) {
                transferDataFile1(oldDataFilePath, currentDirPath);
            }
            //图表不为空，导出图表
            if (StringUtils.isNotEmpty(chartData)) {
                String chartFilePath = currentDirPath + "/" + "模型报表.xlsx";
                String imgPath = basePath + "/" + IMG_DIR + ruleId + ".png";
                ModelResultData resultData = new ModelResultData();
                ruleRunViewService.initFromResult(chartData, resultData);
                //整理数据生成图表
                Map<String, List<String>> chartMap = sortOutData(resultData, chartX, chartVal);
                boolean success = CreateChartUtils.creatChart(chartType, modelName, chartMap, imgPath);
                if (!success) {
                    log.error("生成图表失败");
                    return;
                }
                File excelFile = new File(chartFilePath);
                ExcelWriter writer = ExcelUtil.getWriter(excelFile, modelName);
                ExcelUtils.saveImgToExcel(writer.getWorkbook(), writer.getSheet(), imgPath, 1);
                writer.setSheet("报表数据");
                List<LinkedHashMap<String, String>> reportData = arrangeData(resultData);
                writer.write(reportData);
                writer.flush();
                writer.close();
                FileUtil.del(imgPath);
            }
        });
        String zipPath = baseDirPath + ".zip";
        File zip = ZipUtil.zip(baseDirPath, zipPath, true);
        //删除文件夹及其里面的内容
        FileUtil.del(baseDirPath);
        return zip;
    }

    private List<LinkedHashMap<String, String>> arrangeData(ModelResultData resultData) {
        LinkedHashMap<String, String> titleMap = resultData.getTitle();
        List<Map<String, String>> dataList = resultData.getData();
        ArrayList<LinkedHashMap<String, String>> reList = new ArrayList<>();
        if (titleMap != null && titleMap.size() > 0 && dataList != null && dataList.size() > 0) {
            for (Map<String, String> dataMap : dataList) {
                LinkedHashMap<String, String> reDataMap = new LinkedHashMap<>();
                for (Map.Entry<String, String> entry : titleMap.entrySet()) {
                    String key = entry.getKey();
                    String title = entry.getValue();
                    reDataMap.put(title, dataMap.get(key));
                }
                reList.add(reDataMap);
            }
        }
        return reList;
    }

    private Map<String, List<String>> sortOutData(ModelResultData resultData, String chartX, String chartVal) {
        List<Map<String, String>> dataList = resultData.getData();
        if (dataList == null || dataList.size() < 1) {
            return null;
        }
        List<String> chartXList = new ArrayList<>();
        List<String> chartValList = new ArrayList<>();
        dataList.forEach(map -> {
            String xVal = map.get(chartX);
            String value = map.get(chartVal);
            if (StringUtils.isNotEmpty(xVal) && StringUtils.isNotEmpty(value)) {
                chartXList.add(xVal);
                chartValList.add(value);
            }
        });
        Map<String, List<String>> reMap = new HashMap<>();
        reMap.put("chartX", chartXList);
        reMap.put("chartVal", chartValList);
        return reMap;
    }


    private void transferDataFile(String oldPath, String currentPath) {
        File oldFile = new File(oldPath);
        if (!oldFile.exists() || !oldFile.isFile()) {
            log.error("原数据不存在，oldDataFilePath：{}", oldPath);
            return;
        }
        //获取文件名
        String fileName = oldPath.substring(oldPath.lastIndexOf("/") + 1);
        String curFilePath = currentPath + "/" + fileName;
        if (!fileName.contains(".")) {
            curFilePath += ".txt";
        }
        File file = FileUtil.copy(oldFile, new File(curFilePath), true);
        if (!file.exists() || !file.isFile()) {
            log.error("文件复制失败，oldDataFilePath：{}", oldPath);
            return;
        }
        log.error("文件复制成功，新文件名：curFilePath {}", curFilePath);
    }

    private static void transferDataFile1(String oldPath, String currentPath) {
        File oldFile = new File(oldPath);

        if (oldFile.exists() && oldFile.isFile()) {
            //获取文件名
            String fileName = oldPath.substring(oldPath.lastIndexOf("/") + 1);
            String curFilePath = currentPath + "/" + fileName;
            if (!fileName.contains(".")) {
                curFilePath += ".txt";
            }
            File curFile = new File(curFilePath);
            // 新建文件输入流并对它进行缓冲
            try {
                FileInputStream input = new FileInputStream(oldFile);
                BufferedInputStream inBuff=new BufferedInputStream(input);
                // 新建文件输出流并对它进行缓冲
                FileOutputStream output = new FileOutputStream(curFile);
                BufferedOutputStream outBuff=new BufferedOutputStream(output);
                //加上这个 csv文件用Excel打开就不会乱码
                outBuff.write(new byte []{( byte ) 0xEF ,( byte ) 0xBB ,( byte ) 0xBF });
                // 缓冲数组
                byte[] b = new byte[1024 * 5];
                int len;
                while ((len =inBuff.read(b)) != -1) {
                    outBuff.write(b, 0, len);
                }
                // 刷新此缓冲的输出流
                outBuff.flush();

                //关闭流
                inBuff.close();
                outBuff.close();
                output.close();
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            log.error("原数据不存在，oldDataFilePath：{}", oldPath);
        }
    }

    public static void main(String[] args) {
        transferDataFile1("/Users/qy/Desktop/old.csv", "/Users/qy/Documents/");
    }
}
