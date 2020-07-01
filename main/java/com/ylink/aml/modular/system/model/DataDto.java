package com.ylink.aml.modular.system.model;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


/**
 * 字典值定义表
 *
 * </p>
 *
 * @since 2018-12-07
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class DataDto {

    private static final long serialVersionUID = 1L;



    //private String ruleName;
   private String threename;
    private String title;
    /**
     * 	模型分类第一级值id
     */

    private String modelType1ItemValId;
    /**
     * 模型分类第二级值id
     */
    private String modelType2ItemValId;
    private String ruleId;
    private int type;
    private String VItemValId1;
    private String VItemValId2;

}
