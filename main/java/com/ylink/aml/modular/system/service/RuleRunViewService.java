package com.ylink.aml.modular.system.service;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrSpliter;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ylink.aml.modular.system.dto.ModelResultChart;
import com.ylink.aml.modular.system.dto.ModelResultData;
import com.ylink.aml.modular.system.entity.Model;
import com.ylink.aml.modular.system.entity.RuleRunViewEntity;
import com.ylink.aml.modular.system.mapper.RuleRunViewMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * </p>
 *
 * @since 2019-06-07
 */
@Service
@Slf4j
public class RuleRunViewService extends ServiceImpl<RuleRunViewMapper, RuleRunViewEntity> {
    @Autowired
    private ModelService modelService;

    private static final String HIGH_LIGHT_PREFIX = "mark_";

    /**
     * 返回结果初始化
     *
     * @param resultData
     * @return
     */
    public ModelResultData initFromResult(String resultData, ModelResultData reData) {
        if (StrUtil.isEmpty(resultData)) {
            return reData;
        }
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        String[] rowArray = resultData.split("\n");
        if (rowArray.length < 1) {
            return reData;
        }
        for (int i = 0; i < rowArray.length; i++) {
            List<String> split = StrSpliter.split(rowArray[i], '|', 0, true, false);
            int nFieldIndex = 0;
            if (i == 0) {
                LinkedHashMap<String, String> mapField = new LinkedHashMap<String, String>();
                for (String val : split) {

                    mapField.put("f" + nFieldIndex, val);
                    nFieldIndex++;
                }
                reData.setTitle(mapField);
            } else {
                Map<String, String> map = new HashMap<String, String>();
                for (String val : split) {
                    if (StrUtil.isEmpty(val)) {
                        val = "";
                    }
                    if (!"".equals(val) && val.startsWith("'")) {
                        val = val.substring(1);
                    }
                    map.put("f" + nFieldIndex, val);
                    nFieldIndex++;
                }
                resultList.add(map);
            }
        }
        reData.setCount(resultList.size());
        reData.setData(resultList);
        //根据数据整理显示高亮数据
        orgHighLight(reData);
        return reData;
    }

    private void orgHighLight(ModelResultData reData) {
        //原标题
        LinkedHashMap<String, String> titleMap = reData.getTitle();
        //原数据
        List<Map<String, String>> dataList = reData.getData();
        //提取后的正常列的数据
        List<Map<String, String>> newDataList = new ArrayList<>();
        //提取后正常列的标题
        LinkedHashMap<String, String> newTitleMap = new LinkedHashMap<>();
        //高亮列的索引关系
        LinkedHashMap<String, String> highLightRelationMap = new LinkedHashMap<>();
        //高亮数据的判定数据
        List<Map<String, String>> highLightDataList = new ArrayList<>();
        List<String> highLightIndexList = new ArrayList<>();
        List<int[]> coordinateList = new ArrayList<>();
        //将正常列和高亮标志列 区分出来
        for (Map.Entry<String, String> entry : titleMap.entrySet()) {
            String titleKey = entry.getKey();
            String titleValue = entry.getValue();
            if (titleValue.startsWith(HIGH_LIGHT_PREFIX)) {
                highLightIndexList.add(titleKey);
                String title = titleValue.substring(titleValue.indexOf("_") + 1);
                for (Map.Entry<String, String> cEntry : titleMap.entrySet()) {
                    if (title.equalsIgnoreCase(cEntry.getValue())) {
                        highLightRelationMap.put(cEntry.getKey(), titleKey);
                    }
                }
            } else {
                newTitleMap.put(titleKey, titleValue);
            }
        }
        reData.setTitle(newTitleMap);
        //将正常列的数据提取出来
        if (!highLightRelationMap.isEmpty()) {
            for (Map<String, String> dataMap : dataList) {
                Map<String, String> newDataMap = MapUtil.newHashMap();
                Map<String, String> newHighLightDataMap = MapUtil.newHashMap();
                for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                    if (!highLightIndexList.contains(entry.getKey())) {
                        newDataMap.put(entry.getKey(), entry.getValue());
                    } else {
                        newHighLightDataMap.put(entry.getKey(), entry.getValue());
                    }
                }
                newDataList.add(newDataMap);
                highLightDataList.add(newHighLightDataMap);
            }
            reData.setData(newDataList);
        }
        if (highLightDataList.size() < 1) {
            return;
        }
        //[f3, f4, f5]
        //{f0=AAA, f1=BBB, f2=CCC}
        //newDataList - > [{f0=1, f1=2, f2=3}, {f0=1, f1=1, f2=2}, {f0=66, f1=66, f2=6}]
        //highLightDataList - > [{f3=1, f4=0, f5=1}, {f3=1, f4=1, f5=0}, {f3=0, f4=0, f5=1}]
        //highLightRelationMap - > {f0=f3, f1=f4, f2=f5}
        for (Map.Entry<String, String> relationMap : highLightRelationMap.entrySet()) {
            //数据的索引
            String dataIndex = relationMap.getKey();
            //高亮判断数据的索引
            String highLightIndex = relationMap.getValue();
            for (int i = 0; i < highLightDataList.size(); i++) {
                Map<String, String> highLightDataMap = highLightDataList.get(i);
                String hlValue = highLightDataMap.get(highLightIndex);
                if (StringUtils.isNotEmpty(hlValue) && "1".equals(hlValue)) {
                    int[] coordinateArr = new int[2];
                    coordinateArr[0] = i;
                    coordinateArr[1] = Integer.parseInt(dataIndex.substring(1));
                    coordinateList.add(coordinateArr);
                }
//                for (Map.Entry<String, String> entry : highLightDataMap.entrySet()) {
//                    //高亮判断数据的索引
//                    //根据高亮索引查询对应的值
//                    String hlIndex = entry.getKey();
//                    //高亮判断数据
//                    String hlValue = entry.getValue();
//                    if (highLightIndex.equalsIgnoreCase(hlIndex)) {
//                        //需要显示高亮的数据
//                        if ("1".equals(hlValue)) {
//                            int[] coordinateArr = new int[2];
//                            coordinateArr[0] = i;
//                            coordinateArr[1] = Integer.parseInt(dataIndex.substring(1));
//                            coordinateList.add(coordinateArr);
//                        }
//                    }
//                }
            }
        }
        reData.setCoordinateArr(coordinateList.toArray(new int[coordinateList.size()][]));
    }

    public void initChartValue(Model model, ModelResultChart modelChartResult) {
        modelChartResult.setChartType(model.getChartType());
        if (StrUtil.isEmpty(model.getChartX())) {
            modelChartResult.setChartX("f2");
        } else {
            modelChartResult.setChartX(model.getChartX());
        }
        if (StrUtil.isEmpty(model.getChartVal())) {
            if (modelChartResult.getTitle() != null) {
                modelChartResult.setChartVal("f3");
            }
        } else {
            modelChartResult.setChartVal(model.getChartVal());
        }
    }

    public List<RuleRunViewEntity> getRuleRunList(Integer autoCheckId) {
        return this.baseMapper.selectList(new QueryWrapper<RuleRunViewEntity>()
                .eq("AUTO_CHECK_ID", autoCheckId).orderByAsc("SUBMIT_TIME"));
    }

    /*
     * @Description：根据一键检测批次id查询出所有ruleRunId
     * @param: []
     * @return void
     */
    public List<Object> getRuleRunId(Integer autoCheckId, String ruleId) {
        QueryWrapper<RuleRunViewEntity> wrapper = new QueryWrapper();
        wrapper.select("RULE_RUN_ID").eq("AUTO_CHECK_ID", autoCheckId).eq("RULE_ID", ruleId);
        return baseMapper.selectObjs(wrapper);
    }


    public void exportExcel(HttpServletResponse response, String ruleRunId) throws Exception {
        RuleRunViewEntity ruleRunViewEntity = baseMapper.selectById(ruleRunId);
        String rultpath = ruleRunViewEntity.getRerultPath();
        File file = new File(rultpath);
        String ruleId = ruleRunViewEntity.getRuleId();
        Timestamp submitTime = ruleRunViewEntity.getSubmitTime();
        String dateStr = new SimpleDateFormat("yyyyMMdd hhmmss").format(submitTime);
        String fileName = ruleId + "_" + dateStr + ".csv"; // 文件的默认保存名
        if (!file.exists()) {
            // 让浏览器用UTF-8解析数据
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            response.getWriter().write("文件不存在或已过期,请重新生成");
            return;
        }
        //下载的文件携带这个名称
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        //文件下载类型--二进制文件
        response.setContentType("application/octet-stream");
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] content = new byte[fis.available()];
            fis.read(content);
            fis.close();

            ServletOutputStream sos = response.getOutputStream();
            sos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            sos.write(content);

            sos.flush();
            sos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

       /* response.setCharacterEncoding("UTF-8");
        if (rultpath == null || rultpath == "") {
            response.getWriter().write("生成文件路径失败");
        }*/
        //File file = new File(rultpath);
       /* if (!file.exists()) {
            // 让浏览器用UTF-8解析数据
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            response.getWriter().write("文件不存在或已过期,请重新生成");
            return;
        }*/

        /*response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename="
                + URLEncoder.encode(fileName, "UTF-8"));
        response.setCharacterEncoding("UTF-8");
        InputStream is = null;
        OutputStream  os = null;
        try {
            is = new FileInputStream(rultpath);
            //加上bom头,解决excel打开乱码问题
            byte[] buffer = new byte[1024];
            os = response.getOutputStream();
            int len;
            os.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
                log.info("ossaaaaaaaaaaaaaaaaaaaaaaaaaaa"+os);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (is != null) is.close();
                if (os != null) os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    public void downprog(HttpServletResponse response, HttpServletRequest request, String ruleRunId, String chartTypes) throws Exception {
        RuleRunViewEntity ruleRunViewEntity = baseMapper.selectById(ruleRunId);
        ModelResultChart modelDataResult = new ModelResultChart();
        Model model = modelService.SelectById(ruleRunViewEntity.getRuleId());
        modelDataResult.setChartX(model.getChartX());
        modelDataResult.setChartVal(model.getChartVal());
        ExcelWriter writer = new ExcelWriter(true, "模型圖表基本信息");
        //自定义标题别名

        initFromResult(ruleRunViewEntity.getRerultChartData(), modelDataResult);
        List<Map<String, String>> dataList = transferData(modelDataResult);
        if (dataList == null || dataList.size() < 1) {
            return;
        }
        writer.write(dataList, true);
        if (chartTypes == null) {
            log.error("图表信息为空");
            return;
        }

        Workbook workbook = writer.getWorkbook();
        Sheet sheet = writer.getSheet();
        if (StringUtils.isNotEmpty(chartTypes)) {
            String[] imgUrlArr = chartTypes.split("base64,");//拆分base64编码后部分
            //Base64 decode = new org.bouncycastle.util.encoders.Base64();
            Base64Decoder decode = new Base64Decoder();
            byte[] buffer = decode.decode(imgUrlArr[1]);
            String picPath = request.getSession().getServletContext().getRealPath("") + "/" + UUID.randomUUID().toString() + ".png";
            File file = new File(picPath);//图片文件
            //生成图片
            OutputStream out = new FileOutputStream(file);//图片输出流
            out.write(buffer);
            out.flush();//清空流
            out.close();//关闭流
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(); // 将图片写入流中
            BufferedImage bufferImg = ImageIO.read(new File(picPath));
            ImageIO.write(bufferImg, "PNG", outStream); // 利用HSSFPatriarch将图片写入EXCEL
            Drawing patri = sheet.createDrawingPatriarch();
            //boolean result = ExcelUtils.evaluateReportToExcel(workbook, sheet, chartCode, imgFilePath, dataList.size());
            //boolean result = ExcelUtils.evaluateReportToExcel(workbook, sheet, chartCode, picPath, dataList.size());
            ClientAnchor anchor = new XSSFClientAnchor(5 * 10000, 0, 100, 100, 0, dataList.size() + 2, 10, dataList.size() + 24);
            patri.createPicture(anchor, workbook.addPicture(outStream.toByteArray(), XSSFWorkbook.PICTURE_TYPE_PNG));
            if (file.exists()) {
                file.delete();//删除图片
            }
        }
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        String codedFileName = java.net.URLEncoder.encode("Chart_" + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_FORMAT), "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(codedFileName.getBytes("UTF-8"), "GBK") + ".xls");
        writer.flush(response.getOutputStream());
        // 关闭writer，释放内存
        writer.close();

    }

    List<Map<String, String>> transferData(ModelResultChart resultChart) {
        List<Map<String, String>> reList = new ArrayList<>();
        List<Map<String, String>> dataList = resultChart.getData();
        LinkedHashMap<String, String> title = resultChart.getTitle();
        if (title == null || title.size() < 1) {
            return reList;
        }
        if (dataList == null || dataList.size() < 1) {
            return reList;
        }
        for (Map<String, String> map : dataList) {
            Map<String, String> dataMap = new LinkedHashMap<>();
            for (String key : title.keySet()) {
                String dataValue = map.get(key) == null ? "" : map.get(key);
                String titleValue = title.get(key);
                dataMap.put(titleValue, dataValue);
            }
            reList.add(dataMap);
        }
        return reList;


    }

    public RuleRunViewEntity ifAsNot(String ruleRunId) {
        return baseMapper.selectById(ruleRunId);
    }

    public static void main(String[] args) {
        Map<String, String> map = MapUtil.newHashMap();
        map.put("f0", "AAA");
        map.put("f1", "BBB");
        map.put("f2", "CCC");
//        map.put("f3", "MSK_AAA");
//        map.put("f4", "MSK_BBB");
//        map.put("f5", "MSK_CCC");
        Map<String, String> map1 = new HashMap<>();
        map1.put("f0", "1");
        map1.put("f1", "2");
        map1.put("f2", "3");
//        map1.put("f3", "1");
//        map1.put("f4", "0");
//        map1.put("f5", "1");
        Map<String, String> map2 = new HashMap<>();
        map2.put("f0", "1");
        map2.put("f1", "1");
        map2.put("f2", "2");
//        map2.put("f3", "1");
//        map2.put("f4", "1");
//        map2.put("f5", "0");
        Map<String, String> map3 = new HashMap<>();
        map3.put("f0", "66");
        map3.put("f1", "66");
        map3.put("f2", "6");
//        map3.put("f3", "0");
//        map3.put("f4", "0");
//        map3.put("f5", "1");
        List<Map<String, String>> dataList = new ArrayList<>();
        dataList.add(map1);
        dataList.add(map2);
        dataList.add(map3);
        //将正常列和高亮标志列 区分出来
        LinkedHashMap<String, String> highLightRelationMap = new LinkedHashMap<>();
        List<String> highLightIndexList = new ArrayList<>();
        LinkedHashMap<String, String> newTitleMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String titleKey = entry.getKey();
            String titleValue = entry.getValue();
            if (titleValue.startsWith(HIGH_LIGHT_PREFIX)) {
                highLightIndexList.add(titleKey);
                String title = titleValue.substring(titleValue.indexOf(HIGH_LIGHT_PREFIX) + 4);
                for (Map.Entry<String, String> cEntry : map.entrySet()) {
                    if (title.equalsIgnoreCase(cEntry.getValue())) {
                        highLightRelationMap.put(cEntry.getKey(), titleKey);
                    }
                }
            } else {
                newTitleMap.put(titleKey, titleValue);
            }
        }
        System.out.println("highLightIndexList - > " + highLightIndexList);
        System.out.println("highLightRelationMap - > " + highLightRelationMap);
        System.out.println("newTitleMap - > " + newTitleMap);
        //将正常列的数据提取出来
        List<Map<String, String>> newDataList = new ArrayList<>();
        List<Map<String, String>> highLightDataList = new ArrayList<>();
        if (!highLightRelationMap.isEmpty()) {
            for (Map<String, String> dataMap : dataList) {
                Map<String, String> newDataMap = MapUtil.newHashMap();
                Map<String, String> newHighLightDataMap = MapUtil.newHashMap();
                for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                    if (!highLightIndexList.contains(entry.getKey())) {
                        newDataMap.put(entry.getKey(), entry.getValue());
                    } else {
                        newHighLightDataMap.put(entry.getKey(), entry.getValue());
                    }
                }
                newDataList.add(newDataMap);
                highLightDataList.add(newHighLightDataMap);
            }
        }
        System.out.println("newDataList - > " + newDataList);
        System.out.println("highLightDataList - > " + highLightDataList);
    }
}