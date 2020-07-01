package com.ylink.aml.modular.system.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ylink.aml.modular.system.model.TreeNode;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/7/11 11:44
 */
@Data
public class RuleAutoCheckDto implements Serializable {
    private static final long serialVersionUID = -3076090224799434707L;

    /**
     * 一键检测批次id
     */
    private Integer autoCheckId;
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
    /**
     *  处理状态
     */
    private String status;
    /**
     *  模型执行总数,检查项总数
     */
    private Integer ruleCnt;
    /**
     *  已检查完成数量
     */
    private Integer checkEndNum;
    /**
     *  检查进度
     */
    private Integer checkProgress;
    /**
     *  模型执行结果
     */
    List<RuleRunDto> ruleRunDtoList;
    /**
     *  模型执行结果树
     */
    List<TreeNode> tree;


}
