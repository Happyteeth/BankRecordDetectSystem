package com.ylink.aml.modular.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ylink.aml.modular.system.dto.SaveAsModelDto;

import java.util.Map;

/**
 * @author qy
 */
public interface IExecuteManageService {

    /**
     * 查询列表 -- 分析和模型
     * @param isAll 是否查询全部
     * @param ruleType 数据类型
     * @param fuzzy 模糊字段
     * @param sDate 开始时间
     * @param eDate 结束时间
     * @param field 排序字段
     * @param order 排序方式
     * @return 结果
     */
    Page<Map<String, Object>> list(boolean isAll, String ruleType, String fuzzy, String sDate, String eDate, String field, String order);

    /**
     * 保存分析为模型
     * @param ruleId 模型规则ID/分析ID
     * @param saveAsModelDto 参数
     * @return 成功 or 失败
     */
    boolean saveAsModel(String ruleId, SaveAsModelDto saveAsModelDto);
}
