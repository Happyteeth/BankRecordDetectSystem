package com.ylink.aml.modular.system.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ModelParaEmportDto implements Serializable {
    private static final long serialVersionUID = 259428733812487863L;

    /**
     * 	参数名字符串
     */
    private String paraString;
    /**
     * 参数名说明
     */
    private String paraDesc;
    /**
     * 	参数值
     */
    private String paraValue;


}
