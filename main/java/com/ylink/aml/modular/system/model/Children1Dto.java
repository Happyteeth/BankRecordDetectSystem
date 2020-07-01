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
public class Children1Dto  {

    private static final long serialVersionUID = 1L;


    /**
     * 值的中文名
     */


    private String vItemId;
    private String vItemValId;
    private String title;
    private String firname;
    private String atypeId;
    private String atypevalId;
    private List<DataDto> children;
}
