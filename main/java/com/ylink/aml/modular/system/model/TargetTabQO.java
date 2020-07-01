package com.ylink.aml.modular.system.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/6/28 09:52
 */
@Data
@ApiModel
public class TargetTabQO implements Serializable {

    private static final long serialVersionUID = -3040015960096648330L;

    @ApiModelProperty("目标表名")
    private String tableName;

    @ApiModelProperty("目标表描述")
    private String tableDesc;
}
