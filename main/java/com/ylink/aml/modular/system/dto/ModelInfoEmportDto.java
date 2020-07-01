package com.ylink.aml.modular.system.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class ModelInfoEmportDto implements Serializable {
    private static final long serialVersionUID = 4225880087475113285L;
    /**
     * 规则id
     */
    private String ruleId;
    /**
     * 规则或模型名称
     */
    private String ruleName;
    /**
     * 模型描述
     */
    private String modelDesc;

    /**
     * 页面配置项
     */
    private String ruleWeb;

    /**
     * 规则检测执行语言
     */
    private String taskProgram;

    /**
     * 实际检测的SQL或程序
     */
    private String ruleProg;

    /**
     * 图标的样式
     */
    private String chartType;

    /**
     * 图表SQL或程序
     */
    private String chartProg;

    /**
     * 规则分类
     */
    private String ruleType;
    /**
     * 模型来源
     */
    private String modelSource;
    /**
     * 是否参与一键检测：0 - 不参与；1 - 参与
     */

    private String ifAutoCheck;
    /**
     * 模型分类第一级
     */
    private String modelType1ItemId;

    /**
     * 模型分类第二级
     */

    private String modelType2ItemId;
    /**
     * 删除标志
     */
    private String cDelFlag;

    /**
     * 模型分类第一级值id
     */

    private String modelType1ItemValId;
    /**
     * 模型分类第二级值id
     */
    private String modelType2ItemValId;


    /**
     * 参数配置
     */
    private List<ModelParaEmportDto> listPara;
}
