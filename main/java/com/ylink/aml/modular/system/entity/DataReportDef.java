package com.ylink.aml.modular.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author qy
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("DATA_REPORT_DEF")
public class DataReportDef implements Serializable {

    /**
     * 目标表名
     */
    @TableId(value = "table_name")
    private String tableName;

    /**
     * 统计项名，直接存中文
     */
    @TableId(value = "item_name")
    private String itemName;


    /**
     * 图表的样式：1 - 柱状图（竖）；2 - 柱状图（横）；3 - 饼形图；4 - 折线图
     */
    private String chartType;

    /**
     * 操作时间（新增）
     */
    @TableField(value = "d_insert", fill = FieldFill.INSERT)
    private Date dInsert;
}
