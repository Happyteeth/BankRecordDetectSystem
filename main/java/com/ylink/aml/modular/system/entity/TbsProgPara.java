package com.ylink.aml.modular.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * ���������
 * </p>
 *
 * @author lida123
 * @since 2019-06-28
 */
@Data
@TableName("TBS_PROG_PARA")
public class TbsProgPara implements Serializable {

    private static final long serialVersionUID = -916159332197628844L;
    /**
     * ����ID�������Ի�������
     */
    @TableField("V_PROG_ID")
    private String vProgId;
    /**
     * ����ID�������Ի�������
     */
    @TableField("V_PARA_ID")
    private String vParaId;
    /**
     * ����˵��
     */
    @TableField("V_PARA_DESC")
    private String vParaDesc;
    /**
     * ����ֵ
     */
    @TableField("V_PARA_VALUE")
    private String vParaValue;
    /**
     * ����ʱ�䣨������
     */
    @TableField("D_INSERT")
    private Date dInsert;
    /**
     * ����ʱ�䣨����޸ģ�
     */
    @TableField("D_UPDATE")
    private Date dUpdate;

}
