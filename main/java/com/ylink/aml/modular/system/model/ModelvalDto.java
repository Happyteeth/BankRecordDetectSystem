package com.ylink.aml.modular.system.model;


import lombok.Data;

import java.io.Serializable;


@Data
public class ModelvalDto implements Serializable {


    /**
     * 	模型分类第一级值id
     */

    private String modelType1ItemValId;
    /**
     * 模型分类第二级值id
     */
    private String modelType2ItemValId;

    /**
     * 字典表对应第一级id
     */
    private String vItemValId1;
    /**
     * 	字典表对应第二级id
     */

    private String vItemValId2;
}
