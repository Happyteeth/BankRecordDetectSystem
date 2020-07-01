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
@TableName("TARGET_TAB")
public class TargetTab implements Serializable {

    /**
     * 目标表名
     */
    @TableId(value = "table_name")
    private String tableName;

    /**
     * 目标表说明
     */
    private String tableDesc;

    /**
     * 目标表类型：1 - 人行规定的表；2 - 自定义表
     */
    private String tableType;

    /**
     * 操作时间（新增）
     */
    @TableField(value = "d_insert", fill = FieldFill.INSERT)
    private Date dInsert;

    /**
     * 操作时间（最后修改）
     */
    @TableField(value = "d_update", fill = FieldFill.UPDATE)
    private Date dUpdate;

    /**
     * 操作人（新增）
     */
    @TableField(value = "v_insert_user", fill = FieldFill.INSERT)
    private String vInsertUser;


}
