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
public class Children {

    private static final long serialVersionUID = 1L;


    /**
     * 值的中文名
     */
    private String cid;
    private String cvalid;
    //private String vValName;
    //private String twoname;
    private String title;
    private List<DataDto> DataDto;
    private List<DataDto> Children;
}
