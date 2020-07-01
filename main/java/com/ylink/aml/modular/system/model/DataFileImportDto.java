package com.ylink.aml.modular.system.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/6/21 09:43
 */
@Data
@ApiModel
public class DataFileImportDto implements Serializable {
    private static final long serialVersionUID = -1380718100050334776L;
/*    @ApiModelProperty("文件数组")
    private MultipartFile file [];*/
    @ApiModelProperty("大数据版文件路径")
    private List<String> pathList;

    @ApiModelProperty("导入表名")
    private String tableName;

    @ApiModelProperty("文件格式")
    private String fileFormat;

    @ApiModelProperty("字段分隔符")
    private String delimitField;

    @ApiModelProperty("是否删除首行,1：带标题行，0：不带标题行")
    private String ifTitle;
}
