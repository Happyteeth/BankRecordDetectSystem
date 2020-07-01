package com.ylink.aml.modular.system.service;

import com.ylink.aml.modular.system.model.BatchCalcDto;
import com.ylink.aml.modular.system.model.DataAnalysisParamDto;
import com.ylink.aml.modular.system.model.DataStatisticsParamDto;
import com.ylink.aml.modular.system.model.SaveAnalysisAsModelDto;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author qy
 */
public interface IDataAnalysisService  {

    /**
     * 根据表名和列名查询字典信息
     * @param tableName 表名
     * @param colName 列名
     * @return 结果集
     */
    List<Map<String, Object>> findDicByCol(String tableName, String colName);

    /**
     * 根据选择的参数查询列表
     * @param paramDto 查询条件
     * @return 查询结果
     */
    Map<String, Object> getListBySql(DataAnalysisParamDto paramDto);

    /**
     * 导出查询数据
     * @param paramDto 查询条件
     * @return 是否成功 true or false
     */
    File exportData(DataAnalysisParamDto paramDto);

    /**
     * 数据统计
     * @param paramDto 统计条件参数
     * @return 结果集
     */
    List<Map<String, Object>> dataStatistics(DataStatisticsParamDto paramDto);

    /**
     * 批量计算图表
     * @param calcDto 参数
     * @return 结果
     */
    Map<String, List<Map<String, Object>>> batchCalc(BatchCalcDto calcDto);

    /**
     * 导出计算结果和图表
     * @param paramDto 参数
     * @return 生成的文件
     */
    File exportCalcRes(DataStatisticsParamDto paramDto);

    /**
     * 导出图表 -- 新
     * @param calcDto 参数
     * @return 文件
     */
    File exportCalc(BatchCalcDto calcDto);

    /**
     * 保存分析结果为模型
     * @param analysis 模型
     * @return 保存成功数据
     */
    int saveAnalysisAsModel(SaveAnalysisAsModelDto analysis);

    /**
     * 提交分析
     * @param analysis 参数
     * @return 是否成功 true or false
     */
    boolean commitAnalysis(SaveAnalysisAsModelDto analysis);

}
