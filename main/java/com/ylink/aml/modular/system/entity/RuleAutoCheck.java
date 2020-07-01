package com.ylink.aml.modular.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 一键检测批次定义
 * </p>
 *
 * @author lida
 * @since 2019-07-10
 */
@Data
@TableName("RULE_AUTO_CHECK")
public class RuleAutoCheck implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 一键检测批次ID
     */
    @TableId(value = "AUTO_CHECK_ID", type = IdType.AUTO)
    private Integer autoCheckId;
    /**
     * 处理状态：前端自行定义
     */
    @TableField("STATUS")
    private String status;
    /**
     * 执行模型总数
     */
    @TableField("RULE_CNT")
    private Integer ruleCnt;
    /**
     * 执行完成时间
     */
    @TableField("RUN_TIME")
    private Date runTime;
    /**
     * 操作时间（新增）
     */
    @TableField("D_INSERT")
    private Date dInsert;
    /**
     * 操作人（新增）
     */
    @TableField("V_INSERT_USER")
    private String vInsertUser;

}
