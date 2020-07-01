package com.ylink.aml.modular.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author qy
 */
@TableName("RULE_TARGET_MAPPING")
@Data
public class RuleTargetMapping implements Serializable {

    private static final long serialVersionUID = 8493049726685347347L;

    @TableId(value = "RULE_ID", type = IdType.ID_WORKER)
    private String ruleId;

    @TableId(value = "TABLE_NAME",type = IdType.ID_WORKER)
    private String tableName;
}
