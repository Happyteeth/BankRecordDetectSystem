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
public class DictionaryValAndmodelDto   {

    private static final long serialVersionUID = 1L;

    protected String vItemValIdPar;
    protected String vItemIdPar;
    private String vValName;
    /**
     * 模型分类第一级
     */
    private String modelType1ItemId;

    /**
     * 	模型分类第二级
     */

    private String modelType2ItemId;
    /**
     * 	模型分类第一级值id
     */

    private String modelType1ItemValId;
    /**
     * 模型分类第二级值id
     */
    private String modelType2ItemValId;
    private String ruleId;

    private String ruleName;
    private String threename;
    private String firname;
    private String twoname;
    private String vItemId;
    private String vItemValId;

    private String cvalid;
    private String atypeId;
    private String atypevalId;
    private String atype2Id;
    private String atypeval2Id;
    private List<DictionaryValAndmodelDto> list;
    private List<DictionaryValAndmodelDto> children1s;
}
