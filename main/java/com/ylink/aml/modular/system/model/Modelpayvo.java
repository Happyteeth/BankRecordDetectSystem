package com.ylink.aml.modular.system.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=false)
public class Modelpayvo implements Serializable {
   private String vItemValId1;
    private  String ruleId;
    private   String ruleName;
    private String ifAutoCheck;
    /**
     * 		模型描述
     */
    private String modelDesc;
}
