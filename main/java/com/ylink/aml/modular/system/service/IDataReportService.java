package com.ylink.aml.modular.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ylink.aml.modular.system.entity.DataReport;
import com.ylink.aml.modular.system.model.ExportReportParamsDto;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author qy
 */
public interface IDataReportService extends IService<DataReport> {

    /**
     * 根据表名获取该表的报表分析数据
     * @param tableName 表名
     * @return 数据结果
     */
    Object getReportByTableName(String tableName);

    /**
     * 导出到Excel
     * @param params 参数
     * @param response 响应
     */
    void exportReport(ExportReportParamsDto params, HttpServletResponse response);

    /**
     * 查询时间
     * @return 时间
     */
    Date findReportDate();
}
