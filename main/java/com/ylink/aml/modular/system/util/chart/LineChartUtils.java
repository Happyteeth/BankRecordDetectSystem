package com.ylink.aml.modular.system.util.chart;

import javafx.scene.chart.LineChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * 折线图
 *       <p>
 *       创建图表步骤：<br/>
 *       1：创建数据集合<br/>
 *       2：创建Chart：<br/>
 *       3:设置抗锯齿，防止字体显示不清楚<br/>
 *       4:对柱子进行渲染，<br/>
 *       5:对其他部分进行渲染<br/>
 *       6:使用chartPanel接收<br/>
 *
 *       </p>
 * @author qy
 */

public class LineChartUtils {

    private static DefaultCategoryDataset defaultCategoryDataset () {
        // 标注类别
        String[] categories = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        Vector<Series> series = new Vector<>();
        // 柱子名称：柱子所有的值集合
        series.add(new Series("Tokyo", new Double[] { 49.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4 }));
        series.add(new Series("New York", new Double[] { 83.6, 78.8, 98.5, 93.4, 106.0, 84.5, 105.0, 104.3, 91.2, 83.5, 106.6, 92.3 }));
        series.add(new Series("London", new Double[] { 48.9, 38.8, 39.3, 41.4, 47.0, 48.3, 59.0, 59.6, 52.4, 65.2, 59.3, 51.2 }));
        series.add(new Series("Berlin", new Double[] { 42.4, 33.2, 34.5, 39.7, 52.6, 75.5, 57.4, 60.4, 47.6, 39.1, 46.8, 51.1 }));
        // 1：创建数据集合
        DefaultCategoryDataset dataset = ChartUtils.defaultCategoryDataset(series, categories);
        return dataset;
    }

    public static JFreeChart createChart() {
        // 2：创建Chart[创建不同图形]
        JFreeChart chart = ChartFactory.createLineChart("Monthly Average Rainfall", "", "Rainfall (mm)", defaultCategoryDataset());
        // 3:设置抗锯齿，防止字体显示不清楚
        ChartUtils.setAntiAlias(chart);
        // 4:对柱子进行渲染[[采用不同渲染]]
        ChartUtils.lineRender(chart.getCategoryPlot(), false,true);
        // 5:对其他部分进行渲染
        // X坐标轴渲染
        ChartUtils.setXAixs(chart.getCategoryPlot());
        // Y坐标轴渲染
        ChartUtils.setYAixs(chart.getCategoryPlot());
        // 设置标注无边框
        chart.getLegend().setFrame(new BlockBorder(Color.WHITE));
        return chart;
    }

    public static void main(String[] args) {
        JFreeChart chart = createChart();
//        final JFrame frame = new JFrame();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(1024, 420);
//        frame.setLocationRelativeTo(null);
//
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                // 创建图形
        // 6:使用chartPanel接收
//            ChartPanel chartPanel = new ChartPanel(chart);
//                frame.getContentPane().add(chartPanel);
//                frame.setVisible(true);
//            }
//        });
        try {
            saveAsFile(chart, "/Users/qy/Desktop/ppp.png", 800, 600);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void saveAsFile(JFreeChart chart, String outputPath,
                                  int weight, int height)throws Exception {
        FileOutputStream out = null;
        File outFile = new File(outputPath);
        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }
        out = new FileOutputStream(outputPath);
        // 保存为PNG
        ChartUtilities.writeChartAsPNG(out, chart, weight, height);
        // 保存为JPEG
        // ChartUtilities.writeChartAsJPEG(out, chart, weight, height);
        out.flush();
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                // do nothing
            }

        }
    }

}
