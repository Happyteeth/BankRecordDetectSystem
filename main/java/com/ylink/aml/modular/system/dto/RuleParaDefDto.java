package com.ylink.aml.modular.system.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**

 */
@Data
public class RuleParaDefDto implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 规则id
     */
    private String ruleId;

    /**
     * 参数名字符串
     */
    private String paraString;
    /**
     * 参数名说明
     */
    private String paraDesc;
    /**
     * 参数值
     */
    private String paraValue;

}
