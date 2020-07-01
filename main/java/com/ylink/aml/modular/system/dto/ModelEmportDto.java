package com.ylink.aml.modular.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ModelEmportDto implements Serializable {

    private static final long serialVersionUID = -4948831968433702234L;


    /**
     * 来源系统名
     */
    String source;
    /**
     * 用途
     */
    String  purpose;

    /**
     * 版本
     */
    String version ;

    /**
     * 导出时间
     */
    String time;

    /**
     * 用户
     */
    String user;

    /**
     * 参数
     */
    private List<ModelInfoEmportDto> data;
}
