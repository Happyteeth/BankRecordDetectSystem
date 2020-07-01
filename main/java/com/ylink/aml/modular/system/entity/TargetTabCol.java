package com.ylink.aml.modular.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/6/25 14:36
 */
@TableName("TARGET_TAB_COL")
@Data
public class TargetTabCol implements Serializable {
    private static final long serialVersionUID = 4557866812600900948L;
    /**
     * Ŀ�����
     */
    @TableId("TABLE_NAME")
    private String tableName;
    /**
     * �ֶ���
     */
    @TableField("COLUMN_NAME")
    private String columnName;
    /**
     * �ֶ�˳��
     */
    @TableField("COLUMN_SEQ")
    private Integer columnSeq;
    /**
     * �������ͣ�S - �ַ���N - ��ֵ��T - ���ڻ�ʱ��
     */
    @TableField("DATA_TYPE")
    private String dataType;
    /**
     * ����
     */
    @TableField("DATA_LENGTH")
    private Integer dataLength;
    /**
     * ����
     */
    @TableField("DATA_PRECISION")
    private Integer dataPrecision;
    /**
     * С������λ��
     */
    @TableField("DATA_SCALE")
    private Integer dataScale;
    /**
     * ���ڻ�ʱ�����͵ĸ�ʽ��YYYY - �ꣻMM - �£�DD - �գ�HH - ʱ��MI - �֣�SS - ��
     */
    @TableField("DATE_FORMAT")
    private String dateFormat;
    /**
     * �ֵ���ID
     */
    @TableField("V_ITEM_ID")
    private String vItemId;
    /**
     * ����ʱ�䣨������
     */
    @TableField("D_INSERT")
    private LocalDateTime dInsert;
    /**
     * ����ʱ�䣨����޸ģ�
     */
    @TableField("D_UPDATE")
    private LocalDateTime dUpdate;
    /**
     * �����ˣ�������
     */
    @TableField("V_INSERT_USER")
    private String vInsertUser;

    /**
     * 字段对应字典项
     */
    @TableField(exist=false)
    private Map<String,TbsDictionaryVal> mapDictionaryVal;
}