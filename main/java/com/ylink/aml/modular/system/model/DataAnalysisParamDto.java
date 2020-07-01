package com.ylink.aml.modular.system.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author qy
 */
@Data
public class DataAnalysisParamDto implements Serializable {

    private String tableName;

    private List<String> columnList;

    private List<String> chColumnList;

    private List<ConditionDto> conditionList;

    private String relation;

    private String fileName;

}


