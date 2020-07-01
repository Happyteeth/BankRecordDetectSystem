package com.ylink.aml.modular.system.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/7/11 14:46
 */
@Data
public class RuleRunDto implements Serializable {
    private static final long serialVersionUID = 275120872050448253L;

    /**
     * 规则id
     */
    private String ruleId;
    /**
     * 	规则或模型名称
     */
    private String ruleName;

    /**
     *数据SQL执行结果总记录数,违规条数
     */
    private Integer resultCount;

    /**
     *数据量SQL执行结果总记录数，检测的数据量
     */
    private Integer checkCount;

    /**
     *模型执行状态
     */
    private String status;

    /**
     *模型执行id
     */
    private String ruleRunId;
    /**
     *  违规占比
     */
    private String proportion;

}
