package com.ylink.aml.modular.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 字典值定义表
 *
 * </p>
 *
 * @since 2018-12-07
 */
@Data
@TableName("TBS_DICTIONARY_VAL")
public class TbsDictionaryVal implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id(字典项的值Id)
     */
    @TableId(value = "V_ITEM_VAL_ID", type = IdType.ID_WORKER_STR)
    private String vItemValId;


    /**
     * 字典项id
     */

    private String vItemId;

    /**
     * 父字典项的值Id
     */

    private String vItemValIdPar;
    protected String vItemIdPar;


    /**
     * 值的英文名
     */

    private String vValEname;

    /**
     * 值的中文名
     */

    private String vValName;

    /**
     * 排序Id
     */

    private String nOrderid;


    /**
     * 数值分段下限（闭区间）
     */
    private String nLowerLimit;

    /**
     * 数值分段上限（开区间）
     */
    private String nUpperLimit;
    /**
     * 系数
     */
    private String nRatio;
    /**
     * 启动标志
     */

    private String cDelFlag;
    /**
     * 操作时间（新增）
     */

    private Date dInsert;
    /**
     * 操作时间（最后修改）
     */

    private Date dUpdate;

    /**
     * 操作人（新增）

     */

    private String vInsertUser;

    /**
     * 操作人（最后修改）
     */

    private String vUpdateUser;


    /**
     * 树状的层级
     */
    @TableField(value = "N_LEVEL" )
    private Integer  level;

    @Override
    public String toString() {
        return "TbsDictionaryVal{" +
                "vItemValId='" + vItemValId + '\'' +
                ", vItemId='" + vItemId + '\'' +
                ", vItemValIdPar='" + vItemValIdPar + '\'' +
                ", vValEname='" + vValEname + '\'' +
                ", vValName='" + vValName + '\'' +
                ", nOrderid='" + nOrderid + '\'' +
                ", nLowerLimit='" + nLowerLimit + '\'' +
                ", nUpperLimit='" + nUpperLimit + '\'' +
                ", nRatio='" + nRatio + '\'' +
                ", cDelFlag='" + cDelFlag + '\'' +
                ", dInsert=" + dInsert +
                ", dUpdate=" + dUpdate +
                ", vInsertUser='" + vInsertUser + '\'' +
                ", vUpdateUser='" + vUpdateUser + '\'' +
                '}';
    }
}
