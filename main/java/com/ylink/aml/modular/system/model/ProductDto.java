package com.ylink.aml.modular.system.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProductDto implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = -7537518592927596616L;

    /*
     * 产品id
     */
    private Long productId;

    /*
     * 产品标题
     */
    private String title;

    /*
     * 产品介绍
     */
    private String introduce;

    /*
     * 产品内容
     */
    private String content;

    /*
     * 产品状态
     */
    private String status;

}

