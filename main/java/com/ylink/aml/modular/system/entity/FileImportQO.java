package com.ylink.aml.modular.system.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description:文件导入qo
 * @Auther: lida
 * @Date: 2019/6/18 10:33
 */
@Data
@ApiModel
public class FileImportQO implements Serializable {
    private static final long serialVersionUID = -3940110436827000798L;

    @ApiModelProperty("文件路径")
    private List<String> pathList;

    @ApiModelProperty("导入表名")
    private String tableName;

    @ApiModelProperty("文件格式")
    private String format;

    @ApiModelProperty("分隔符")
    private String separator;

    @ApiModelProperty("是否删除首行,1：是，0：否")
    private Integer flag;
}
