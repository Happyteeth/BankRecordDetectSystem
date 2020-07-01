package com.ylink.aml.modular.system.dto;

import lombok.Data;
import org.stringtemplate.v4.ST;

import java.io.Serializable;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/8/29 10:52
 */
@Data
public class CheckFileDto implements Serializable {
    private Boolean bool;
    private String errMess;
}
