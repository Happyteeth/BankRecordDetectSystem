package com.ylink.aml.modular.system.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author qy
 */
@Data
public class BatchCalcDto implements Serializable {

    private String tableName;

    private List<ConditionDto> conditionList;

    /**
     * 统计条件关联关系 and/or
     */
    private String relation;

    private String chartCode;

    private List<CalcDto> list;


}
