package com.ylink.aml.modular.system.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;
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
@TableName("RULE_RUN_PARA")
public class Rulerunpara implements Serializable {

    private static final long serialVersionUID = 1L;


    //执行id
    @TableId(value = "rule_run_id", type = IdType.UUID)
    private String ruleRunId;

    /**
     * 	参数名字符串
     */
    private String paraString;

    /**
     * 	执行的参数值
     */
    private String paraValue;

    /**
     * 操作时间（新增）
     */
    private Date dInsert;



}
