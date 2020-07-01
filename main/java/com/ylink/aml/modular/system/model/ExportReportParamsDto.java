package com.ylink.aml.modular.system.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author qy
 */
@Data
public class ExportReportParamsDto implements Serializable {

        private String tableName;

        private String chTableName;

        private List<Map<String, String>> chartTypes;
}
