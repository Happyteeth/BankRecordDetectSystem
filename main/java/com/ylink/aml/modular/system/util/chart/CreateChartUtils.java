package com.ylink.aml.modular.system.util.chart;

import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author qy
 */
@Slf4j
public class CreateChartUtils {


    public static boolean creatChart(String chartType, String title, Map<String, List<String>> dataMap, String chartFilePath) {
        if (dataMap == null || dataMap.isEmpty() || dataMap.size() < 1) {
            return false;
        }
        List<String> chartXList = dataMap.get("chartX");
        List<String> chartValList = dataMap.get("chartVal");
        if (chartXList == null || chartXList.size() < 1) {
            log.error("x轴数据为空, dataMap : {}", dataMap);
            return false;
        }
        if (chartValList == null || chartValList.size() < 1) {
            log.error("报表数据为空, dataMap : {}", dataMap);
            return false;
        }
        String[] categories = new String[chartXList.size()];
        chartXList.toArray(categories);
        Vector<Series> series = new Vector<>();
        String[] values = new String[chartValList.size()];
        chartValList.toArray(values);
        series.add(new Series("f", values));
        DefaultCategoryDataset dataset = ChartUtils.defaultCategoryDataset(series, categories);
        JFreeChart chart = null;
        switch (chartType) {
            case "1" :
            case "2" :
                //柱状图
                chart = ChartFactory.createBarChart(title, "", "", dataset);
                // 3:设置抗锯齿，防止字体显示不清楚
                ChartUtils.setAntiAlias(chart);
                // 4:对柱子进行渲染[[采用不同渲染]]
                ChartUtils.setBarRenderer(chart.getCategoryPlot(), false);
                // X坐标轴渲染
                ChartUtils.setXAixs(chart.getCategoryPlot());
                // Y坐标轴渲染
                ChartUtils.setYAixs(chart.getCategoryPlot());
                break;
            case "3" :
                //饼状图
                DefaultPieDataset pieDataset = ChartUtils.defaultPieDataset(categories, values);
                chart = ChartFactory.createPieChart(title, pieDataset);
                ChartUtils.setAntiAlias(chart);
                ChartUtils.setPieRender(chart.getPlot());
                break;
            case "4" :
                //折线图
                chart = ChartFactory.createLineChart(title, "", "", dataset);
                ChartUtils.setAntiAlias(chart);
                ChartUtils.lineRender(chart.getCategoryPlot(), false, true);
                // X坐标轴渲染
                ChartUtils.setXAixs(chart.getCategoryPlot());
                // Y坐标轴渲染
                ChartUtils.setYAixs(chart.getCategoryPlot());
                break;
                default:
                    break;
        }
        // 设置标注无边框
        chart.getLegend().setFrame(new BlockBorder(Color.WHITE));
        return saveAsFile(chart, chartFilePath, 800, 600);
    }

    public static boolean saveAsFile(JFreeChart chart, String filePath, int weight, int height) {
        log.info("图表路径：imgPath：{}", filePath);
        FileOutputStream outputStream;
        File outFile = new File(filePath);
        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }
        try {
            outputStream = new FileOutputStream(filePath);
            ChartUtilities.writeChartAsPNG(outputStream, chart, weight, height);
            outputStream.close();
        } catch (FileNotFoundException e) {
            log.error("创建文件失败，filePath：{}", filePath);
            return false;
        } catch (IOException e) {
            log.error("生成本地图片失败，filePath：{}", filePath);
            return false;
        }
        return true;
    }
}
