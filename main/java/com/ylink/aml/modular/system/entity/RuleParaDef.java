package com.ylink.aml.modular.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author ian
 * @since 2019-6-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("RULE_PARA_DEF")
public class RuleParaDef implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则id
     */
    @TableId(value = "rule_id", type = IdType.ID_WORKER)
    private String ruleId;
/**
 * 	参数名字符串
 */
 private String paraString;
    /**
     * 参数名说明
     */
    private String paraDesc;
    /**
     * 	参数值
     */
    private String paraValue;

    /**
     * 	操作时间（新增）
     */
    private Date dInsert;

    /**
     * 操作时间（最后修改）
     */
    private Date dUpdate;

    /**
     * 	操作人（新增）
     */
    private String vInsertUser;

    /**
     * 操作人（最后修改）
     */
    private String  vUpdateUser;


}
