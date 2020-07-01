package com.ylink.aml.modular.system.dto;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class ModelResultChart  extends  ModelResultData{

    /**
     *   图表的样式：1 - 柱状图（竖）；2 - 柱状图（横）；3 - 饼形图
     */
   String  chartType;

    /**
     * 图表的X轴对应的字段别名
     */
   String  chartX;

    /**
     * 图表的数据项对应的字段别名
     */
   String  chartVal;
}
