package com.ylink.aml.modular.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author qy
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("DATA_REPORT")
public class DataReport implements Serializable {

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
     * 维度，如果是字典，转换为对应的中文再存入
     */
    @TableId(value = "cubeName")
    private String cubeName;

    /**
     * 统计值
     */
    private BigDecimal val;

    /**
     * 统计值2
     */
    private BigDecimal val2;

    /**
     * 操作时间（新增）
     */
    private Date dInsert;

}
