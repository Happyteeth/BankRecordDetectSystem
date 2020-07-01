package com.ylink.aml.modular.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/7/26 15:43
 */
@Data
public class FileDto implements Serializable {
    private static final long serialVersionUID = 6460684087633226894L;
    /**
     * ԭ文件名
     */
    private String fileName;
    /**
     * ԭ文件类型
     */
    private String type;
    /**
     * ԭ文件大小
     */
    private Long size;
    /**
     * ԭ文件时间
     */
    private Date time;
    /**
     * ԭ文件路径
     */
    private String path;
    /**
     * ԭ上传标志，
     */
    private String uploadFlag;
}
