package com.ylink.aml.modular.system.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/7/11 14:46
 */
@Data
public class CountDto implements Serializable {
    private static final long serialVersionUID = 275120872050448253L;
    /**
     *  违规占比
     */
    private String proportion;
}
